#!/usr/bin/env node
/**
 * Brain-dump → task structuring prompt test harness.
 * Runs every sample in test/samples/ through the Claude prompt,
 * validates against task.schema.json, and prints pass/fail + tasks.
 *
 * Usage:
 *   ANTHROPIC_API_KEY=sk-... node test/run.mjs
 *
 * Optional env vars:
 *   MODEL          override model (default: claude-sonnet-4-6)
 *   SAMPLE         run a single sample by filename, e.g. SAMPLE=01-voice-booking-webapp.txt
 */

import Anthropic from "@anthropic-ai/sdk";
import Ajv from "ajv";
import { readFileSync, readdirSync } from "fs";
import { resolve, dirname, join } from "path";
import { fileURLToPath } from "url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const root = resolve(__dirname, "..");

// ── Config ────────────────────────────────────────────────────────────────────

const MODEL = process.env.MODEL ?? "claude-sonnet-4-6";
// Support both standard API keys and Claude Code's OAuth Bearer tokens.
// In Claude Code remote environments ANTHROPIC_API_KEY may be absent; instead
// a Bearer token lives in CLAUDE_SESSION_INGRESS_TOKEN_FILE.
let API_KEY = process.env.ANTHROPIC_API_KEY;
let AUTH_HEADER = null; // if set, used instead of x-api-key

if (!API_KEY && process.env.CLAUDE_SESSION_INGRESS_TOKEN_FILE) {
  try {
    const { readFileSync: rfs } = await import("fs");
    const token = rfs(process.env.CLAUDE_SESSION_INGRESS_TOKEN_FILE, "utf8").trim();
    API_KEY = "placeholder"; // SDK requires a non-empty key; we override the header below
    AUTH_HEADER = `Bearer ${token}`;
  } catch {
    // ignore
  }
}

if (!API_KEY) {
  console.error(
    "ERROR: ANTHROPIC_API_KEY is not set.\n" +
    "Set it in the environment before running:\n" +
    "  ANTHROPIC_API_KEY=sk-ant-... node test/run.mjs"
  );
  process.exit(1);
}

const TODAY = new Date().toISOString().slice(0, 10); // YYYY-MM-DD

// ── Load prompt ───────────────────────────────────────────────────────────────

const promptDoc = readFileSync(
  join(root, "prompt/structuring-prompt.md"),
  "utf8"
);

function extractBlock(doc, label) {
  const re = new RegExp(
    "## " + label + "\\s*```[^\\n]*\\n([\\s\\S]*?)```",
    "m"
  );
  const m = doc.match(re);
  if (!m) throw new Error(`Could not find "${label}" block in prompt doc`);
  return m[1].trim();
}

const SYSTEM_TEMPLATE = extractBlock(promptDoc, "System prompt");
const USER_TEMPLATE = extractBlock(promptDoc, "User prompt");

function buildSystemPrompt(date) {
  return SYSTEM_TEMPLATE.replace(/\{\{current_date\}\}/g, date);
}

function buildUserPrompt(text) {
  return USER_TEMPLATE.replace(/\{\{extracted_text\}\}/g, text);
}

// ── Load schema ───────────────────────────────────────────────────────────────

const schema = JSON.parse(
  readFileSync(join(root, "schema/task.schema.json"), "utf8")
);
const ajv = new Ajv({ allErrors: true });
const validate = ajv.compile(schema);

// ── Samples ───────────────────────────────────────────────────────────────────

const samplesDir = join(__dirname, "samples");
let sampleFiles = readdirSync(samplesDir)
  .filter((f) => f.endsWith(".txt"))
  .sort();

if (process.env.SAMPLE) {
  sampleFiles = sampleFiles.filter((f) => f === process.env.SAMPLE);
  if (sampleFiles.length === 0) {
    console.error(`No sample found matching: ${process.env.SAMPLE}`);
    process.exit(1);
  }
}

// ── Claude client ─────────────────────────────────────────────────────────────

const clientOpts = { apiKey: API_KEY };
if (AUTH_HEADER) {
  // Override the x-api-key with a Bearer token by intercepting fetch
  const bearerHeader = AUTH_HEADER;
  clientOpts.fetch = (url, init) => {
    const headers = new Headers(init?.headers ?? {});
    headers.delete("x-api-key");
    headers.set("authorization", bearerHeader);
    return fetch(url, { ...init, headers });
  };
}
const client = new Anthropic(clientOpts);

async function callClaude(sampleText) {
  const message = await client.messages.create({
    model: MODEL,
    max_tokens: 2048,
    system: buildSystemPrompt(TODAY),
    messages: [{ role: "user", content: buildUserPrompt(sampleText) }],
  });
  const raw = message.content[0]?.text ?? "";
  return raw.trim();
}

// ── Validation ────────────────────────────────────────────────────────────────

function parseAndValidate(raw, filename) {
  let parsed;
  try {
    parsed = JSON.parse(raw);
  } catch (e) {
    return {
      ok: false,
      error: `JSON parse failed: ${e.message}`,
      raw,
      tasks: null,
    };
  }

  const valid = validate(parsed);
  if (!valid) {
    return {
      ok: false,
      error: `Schema validation failed:\n  ${ajv.errorsText(validate.errors, { separator: "\n  " })}`,
      raw,
      tasks: parsed,
    };
  }

  return { ok: true, error: null, raw, tasks: parsed };
}

// ── Runner ────────────────────────────────────────────────────────────────────

const PASS = "\x1b[32m✓ PASS\x1b[0m";
const FAIL = "\x1b[31m✗ FAIL\x1b[0m";
const DIM = "\x1b[2m";
const RESET = "\x1b[0m";

let passed = 0;
let failed = 0;

console.log(`\nBrain-Dump Task Structuring — Test Run`);
console.log(`Model: ${MODEL}  |  Date: ${TODAY}`);
console.log("=".repeat(60));

for (const filename of sampleFiles) {
  const sampleText = readFileSync(join(samplesDir, filename), "utf8");
  console.log(`\n▶ ${filename}`);

  let raw;
  try {
    raw = await callClaude(sampleText);
  } catch (e) {
    console.log(`${FAIL}  API error: ${e.message}`);
    failed++;
    continue;
  }

  const { ok, error, tasks } = parseAndValidate(raw, filename);

  if (ok) {
    console.log(`${PASS}  ${tasks.length} task(s) extracted`);
    if (tasks.length === 0) {
      console.log(`  ${DIM}(no actionable items — expected for ramble samples)${RESET}`);
    }
    for (const t of tasks) {
      const due = t.due ? `  [due: ${t.due}]` : "";
      console.log(`  • ${t.title}${due}`);
      if (t.notes) {
        console.log(`    ${DIM}${t.notes.slice(0, 120)}${t.notes.length > 120 ? "…" : ""}${RESET}`);
      }
    }
    passed++;
  } else {
    console.log(`${FAIL}  ${error}`);
    console.log(`  ${DIM}Raw output: ${raw.slice(0, 300)}${RESET}`);
    failed++;
  }
}

console.log("\n" + "=".repeat(60));
console.log(
  `Results: ${passed} passed, ${failed} failed out of ${sampleFiles.length} samples`
);

if (failed > 0) {
  process.exit(1);
}
