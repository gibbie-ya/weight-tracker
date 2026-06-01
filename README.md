# Brain-Dump → Google Tasks

Drop a voice note, photo, PDF, or text file into a Google Drive folder on your Android phone. Structured to-dos appear in Google Tasks automatically. Zero manual steps after setup.

---

## How it works

```
Android share sheet
      ↓
Google Drive: /Brain Dump/To action
      ↓  (Make.com watches this folder)
Download file
      ↓
Router (branch by file type)
  ├─ Audio   → AssemblyAI → transcript text
  ├─ Image   → Claude (reads image) → extracted text
  ├─ PDF/doc → Claude (reads PDF)   → extracted text
  └─ Text    → read directly
      ↓
Anthropic Claude (structuring prompt)
  → strict JSON array of { title, notes, due }
      ↓
Iterator → Google Tasks "Create a Task" (one per item)
      ↓
Google Drive: move file to /Brain Dump/Actioned
```

---

## Prerequisites

| Requirement | Notes |
|---|---|
| Make.com account | Free tier works; Core+ gives 1-min polling |
| Google account | Drive + Tasks |
| AssemblyAI API key | For audio transcription — free tier: 100 hrs/month |
| Anthropic API key | For images, PDFs, AND task structuring — Claude does all three |
| Node.js ≥ 18 | For running the local test harness only |

---

## Part 1 — Google Cloud Setup

### 1.1 Enable APIs

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a project (or select an existing one)
3. Navigate to **APIs & Services → Library**
4. Enable:
   - **Google Drive API**
   - **Google Tasks API**

### 1.2 Create OAuth credentials (for Make.com)

Make.com handles OAuth2 for you when you connect your Google account — you do **not** need to create credentials manually unless you want a custom OAuth app. When connecting in Make, click "Add" on the Google connection and follow the Google sign-in flow; Make uses its own registered OAuth client.

> If you prefer your own OAuth app (for privacy): create a **Desktop** OAuth 2.0 client ID, download the JSON, and enter the client ID + secret when prompted by Make's custom connection option.

---

## Part 2 — Google Drive Folders

1. Open Google Drive on desktop or Android
2. Create the following folder structure:
   ```
   My Drive/
   └── Brain Dump/
       ├── To action/
       └── Actioned/
   ```
3. Note the folder IDs from the URL when you open each folder (the long string after `/folders/`). You'll need these in Make.

### Android: sharing files into "To action"

**Voice note:**
1. Open your voice recorder app (Google Recorder, Samsung Voice Recorder, etc.)
2. Record your note
3. Tap **Share** → **Drive** → navigate to `Brain Dump/To action` → **Save**

**Photo / screenshot:**
1. Open Google Photos or your gallery
2. Long-press the image → **Share** → **Drive** → `Brain Dump/To action`

**PDF or document:**
1. Open the file in any app
2. Tap the share icon → **Drive** → `Brain Dump/To action`

**Quick text note:**
1. Use Google Keep, or any notes app
2. Share the note as text to Drive, or create a `.txt` file and save directly to `Brain Dump/To action`

---

## Part 3 — Make.com Connections

### 3.1 Connect Google (Drive + Tasks)

1. In Make, go to **Connections** → **Add a connection** → search **Google Drive**
2. Sign in with your Google account
3. Grant all requested permissions (Drive, Tasks)
4. This single connection covers both Drive and Tasks modules

### 3.2 Connect AssemblyAI

1. Sign up at [assemblyai.com](https://www.assemblyai.com) — free tier gives 100 hours/month
2. Copy your API key from the dashboard
3. In Make: **Connections** → **Add** → **AssemblyAI** → paste API key → Save

### 3.3 Connect Anthropic (Claude)

1. **Connections** → **Add** → **Anthropic** (or **Claude**)
2. Paste your Anthropic API key
3. Save

> No OpenAI connection needed — Claude reads images and PDFs directly. In Make, the Claude module is called **"Create a prompt"** under the Anthropic Claude app.

---

## Part 4 — Build the Make Scenario

Follow the detailed step-by-step instructions in [`make/manual-build.md`](make/manual-build.md).

**Summary of modules in order:**

1. **Google Drive** — Watch Files in a Folder (`/Brain Dump/To action`)
2. **Google Drive** — Download a File
3. **Router** (4 branches by MIME type):
   - Audio → **AssemblyAI** — Transcribe a Recording
   - Image → **Anthropic Claude** — Create a prompt (reads the image)
   - PDF/doc → **Anthropic Claude** — Create a prompt (reads the PDF)
   - Text → **Tools** — Set Variable (read bytes as string)
4. **Anthropic Claude** — Create a prompt (the structuring prompt → JSON)
5. **JSON** — Parse JSON
6. **Flow control** — Iterator
7. **Google Tasks** — Create a Task
8. **Google Drive** — Move a File (to `/Brain Dump/Actioned`)

See `make/manual-build.md` for exact field mappings, filter conditions, and error handling.

> A structural scaffold (`make/blueprint.json`) is provided, but Make blueprint format changes frequently — treat it as a starting point and re-map connections and fields after import.

---

## Part 5 — The Claude Structuring Prompt

The prompt lives in [`prompt/structuring-prompt.md`](prompt/structuring-prompt.md).

**What it does:** Takes normalised text (transcript, OCR, extracted text, or plain text) and returns a strict JSON array:

```json
[
  {
    "title": "Book car MOT",
    "notes": "Due mid-June, approximately the 15th",
    "due": "2026-06-15"
  },
  {
    "title": "Reply to brother about July visit",
    "notes": "He asked about coming to stay in July",
    "due": null
  }
]
```

**Key rules:**
- Strict JSON only — no prose, no markdown fences
- One task per distinct action
- Titles: short imperative, max 60 characters
- Due dates: ISO `YYYY-MM-DD` inferred from natural language, or `null`
- Returns `[]` if nothing is actionable

---

## Part 6 — Local Testing

Test the prompt against all 8 sample inputs before deploying.

### 6.1 Install dependencies

```bash
npm install
```

### 6.2 Run all samples

```bash
ANTHROPIC_API_KEY=sk-ant-... npm test
```

### 6.3 Run a single sample

```bash
ANTHROPIC_API_KEY=sk-ant-... SAMPLE=02-multi-topic-with-dates.txt node test/run.mjs
```

### 6.4 Expected results by sample

| Sample | Expected |
|---|---|
| `01-voice-booking-webapp.txt` | 3–5 tasks: write up idea, research competitors, check Supabase pricing, write spec |
| `02-multi-topic-with-dates.txt` | 4 tasks with due dates: car insurance (by Friday ~Jun 5), mum's birthday gift (~Jun 14), send design files (today), book leaving do dinner (~Jun 12) |
| `03-pure-ramble-no-actions.txt` | `[]` — no actionable items |
| `04-meeting-notes-actions.txt` | 5 tasks with due dates matching the meeting notes |
| `05-screenshot-email-itinerary.txt` | 1–2 tasks: download e-tickets, check Delay Repay if needed |
| `06-pdf-mixed-info-and-actions.txt` | 6–8 tasks: insurance renewal, GDPR register, fix website address, update bios, blog post, re-engagement email, client survey, invoice final demand, accountant meeting, replace kettle, order forms |
| `07-mixed-personal-work.txt` | 6–7 tasks: finish proposal (Jun 4), chase Marcus, book physio, book MOT (~Jun 15), fix tap/call plumber, reply to brother |
| `08-long-doc-few-actionable.txt` | 2 tasks: order Charaka Samhita, update intake form |

---

## Part 7 — End-to-End Test (per input type)

Once the scenario is built and active, test each input type:

### Voice note
1. Record a short voice note: *"Remind me to buy milk tomorrow and call the dentist next week"*
2. Share to `Brain Dump/To action` as `.m4a`
3. Wait for Make to run (up to 15 min on Free, ~1 min on Core+)
4. Check Google Tasks — expect 2 tasks with appropriate due dates
5. Check `Brain Dump/Actioned` — file should have moved

### Image / screenshot
1. Take a screenshot of a to-do list or handwritten note
2. Share to `Brain Dump/To action`
3. Verify tasks appear in Google Tasks

### PDF
1. Share any PDF (receipt, document, email saved as PDF) to `Brain Dump/To action`
2. Verify text extraction and tasks

### Plain text
1. Create a `.txt` file with a few action items
2. Save to `Brain Dump/To action`
3. Verify tasks

---

## Part 8 — Error Handling

### Empty or garbled extraction
Claude returns `[]` for content with no actionable items — this is correct. No tasks are created, but the file **is** moved to Actioned (it was processed, just produced nothing). If this is undesirable, add a filter before the Move module: only move if the JSON array is non-empty.

### Invalid JSON from Claude
The JSON Parse module will fail. With **Break** error handling on the Router:
- The scenario run fails
- The file stays in `To action` (Move never runs)
- You receive an error notification in Make's run history

Add an error handler that emails you the raw Claude output so you can debug the prompt.

### Unsupported file type
The fallback Router branch (plain text) catches everything not matched by the first three branches. If the file is binary and not readable as text, Claude will receive garbled content and likely return `[]`. The file will still move to Actioned. To prevent this, add a filter on the fallback branch that only allows MIME types starting with `text/`.

### File stays in "To action"
This is the desired failure mode. It means the file was not successfully processed. Check Make's run history for the error. Common causes:
- API key expired or quota exceeded
- Claude returned non-JSON text (prompt injection from content)
- File was too large for the API

---

## Part 9 — Cost Estimates (per file)

Estimates based on typical input sizes. Actual costs vary.

| Input type | Processing | Estimated cost |
|---|---|---|
| Short voice note (30s) | AssemblyAI (free tier) | $0.000 |
| Long voice note (5 min) | AssemblyAI (free tier) | $0.000 |
| Image / screenshot | Claude Haiku reads image (~800 tokens) | ~$0.002 |
| PDF (1–5 pages) | Claude reads PDF (~2000 tokens) | ~$0.008 |
| Plain text | None | $0.000 |
| Claude structuring (all types) | ~500 input + 300 output tokens | ~$0.005 |

**Typical per-dump cost: $0.005–$0.013** (audio is free on AssemblyAI's tier)

---

## Repository structure

```
.
├── prompt/
│   └── structuring-prompt.md     # System + user prompt for Claude
├── schema/
│   └── task.schema.json          # JSON Schema for output validation
├── test/
│   ├── run.mjs                   # Test harness (Node.js)
│   └── samples/
│       ├── 01-voice-booking-webapp.txt
│       ├── 02-multi-topic-with-dates.txt
│       ├── 03-pure-ramble-no-actions.txt
│       ├── 04-meeting-notes-actions.txt
│       ├── 05-screenshot-email-itinerary.txt
│       ├── 06-pdf-mixed-info-and-actions.txt
│       ├── 07-mixed-personal-work.txt
│       └── 08-long-doc-few-actionable.txt
├── make/
│   ├── manual-build.md           # Step-by-step Make GUI build guide
│   └── blueprint.json            # Importable scaffold (re-map after import)
├── package.json
└── README.md
```

---

## Security notes

- Never commit API keys. Use environment variables or Make's connection store.
- The `ANTHROPIC_API_KEY` in the test harness is read from the environment only.
- Make stores credentials encrypted — do not put keys in scenario module fields directly.
