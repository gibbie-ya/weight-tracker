# Make.com — Manual Scenario Build Guide

Step-by-step instructions for building the Brain-Dump → Google Tasks scenario in the Make GUI.

---

## Prerequisites

- Make.com account (Free tier works for low volume; Core+ recommended for instant triggers)
- Google account with Drive and Tasks enabled
- OpenAI API key (for Whisper transcription and image vision)
- Anthropic API key (for Claude task structuring)
- Google Drive folders already created:
  - `/Brain Dump/To action`
  - `/Brain Dump/Actioned`

---

## Step 1 — Create a new scenario

1. Log in to Make.com → **Create a new scenario**
2. Name it: `Brain Dump → Google Tasks`
3. Leave the schedule as-is for now (you'll set it at the end)

---

## Step 2 — Module 1: Trigger — Watch Files in a Folder

1. Click the **+** button → search **Google Drive** → select **Watch Files in a Folder**
2. **Connection:** Connect your Google account (OAuth2). Grant Drive and Tasks permissions.
3. **Folder:** Click the folder picker → navigate to `/Brain Dump/To action`
4. **Watch:** New files only
5. **Maximum number of files:** `1` (process one file per run to keep things simple)
6. Click **OK**

> After saving, Make will ask which file to start watching from. Choose **"From now on"**.

---

## Step 3 — Module 2: Download a File

1. Click **+** after the trigger → **Google Drive** → **Download a File**
2. **Connection:** same Google connection
3. **File ID:** map from the trigger: `{{1.id}}`
4. Click **OK**

---

## Step 4 — Module 3: Router

1. Click **+** → search **Flow control** → select **Router**
2. The Router creates multiple branches. You will create **4 branches** (audio, image, PDF/doc, text).

### Branch 1 — Audio

1. Click the **+** on the first branch coming out of the Router
2. **Filter label:** `Audio`
3. **Condition:** `{{1.mimeType}}` **matches pattern (regex):** `^audio/`
   - OR as a fallback: `{{1.name}}` **matches pattern:** `\.(m4a|mp3|ogg|wav|aac)$`
4. **Module:** OpenAI → **Create a Transcription (Whisper)**
   - Connection: your OpenAI connection
   - File: map `{{2.data}}` (the downloaded bytes from Module 2)
   - File name: map `{{1.name}}`
   - Model: `whisper-1`
   - Response format: `text`
5. **Output variable name** (for convergence): note that this branch outputs `{{3.text}}`

### Branch 2 — Image

1. Click **+** on the second Router branch
2. **Filter label:** `Image`
3. **Condition:** `{{1.mimeType}}` **matches pattern:** `^image/`
   - OR: `{{1.name}}` **matches pattern:** `\.(jpg|jpeg|png|gif|webp|heic|heif)$`
4. **Module:** OpenAI → **Create a Chat Completion**
   - Connection: your OpenAI connection
   - Model: `gpt-4o`
   - Messages → Role: `user`
   - Content type: `Image + text`
     - Text: `Please extract and transcribe all text visible in this image. If there is no text, describe what actions or tasks are implied by the image content.`
     - Image: map `{{2.data}}` (base64 file data), MIME type: `{{1.mimeType}}`
5. **Output:** `{{4.choices[].message.content}}` — you may need a **Text Aggregator** module after this to collapse the array to a single string if Make returns an array.

> **Alternative:** Use the Anthropic module here instead with the vision beta. Either works; OpenAI gpt-4o is more reliable for pure OCR.

### Branch 3 — PDF / Document

1. Click **+** on the third Router branch
2. **Filter label:** `PDF or Document`
3. **Condition:** `{{1.mimeType}}` **matches pattern:** `^application/(pdf|msword|vnd\.openxmlformats|vnd\.ms)`
   - OR: `{{1.name}}` **matches pattern:** `\.(pdf|doc|docx|txt|md)$`
   - **Note:** plain `.txt` files may hit this branch if their MIME is `text/plain` — that is fine; the text extraction below still works.

4. **Module:** OpenAI → **Create a Chat Completion**
   - Model: `gpt-4o` (supports PDF via file upload in the API; alternatively use the Files API)
   - For PDFs: Upload the file bytes as a user message attachment.
   - Messages → Role: `user`, Content: `Extract all text from this document. Return plain text only.`
   - Attach: `{{2.data}}` with MIME `{{1.mimeType}}`

> **Simpler alternative for PDFs:** Use the **PDF.co** or **Adobe PDF Services** Make module to extract text, then pass that text directly to Claude. This avoids passing large binaries to OpenAI.

### Branch 4 — Plain Text

1. Click **+** on the fourth Router branch (or set this as the **else / fallback** branch with no filter)
2. **Filter label:** `Plain text (fallback)`
3. **No condition** (this catches everything not matched above)
4. **Module:** **Tools** → **Set Variable** (or use a Text Aggregator)
   - Variable name: `plain_text`
   - Variable value: convert the downloaded file bytes to text: `{{toString(2.data)}}`
   - If Make doesn't auto-decode, use: **Tools → Base64 Decode** or a **Text Parser → Convert encoding** module.

---

## Step 5 — Module: Converge branches → Set the text variable

After each Router branch produces its text, you need a single value for the Claude step. The cleanest approach in Make:

1. Each branch feeds into the **next module directly** — no explicit converge node needed in Make's Router; branches are independent paths that each continue to their own next modules.
2. **Duplicate the Claude module** into each branch (steps 6–8 below), OR use the following trick:

**Trick: Use a single variable before branching**
- After the Router, before the branch-specific modules, add a **Set Variable** placeholder. Then at the end of each branch, set a module-level variable `extracted_text` to the branch output, and point all branches to the same downstream Claude module.

In practice, the simplest Make approach is to **put the Claude + Iterator + Google Tasks + Move modules inside each branch** — it's verbose but reliable. The guide below describes the shared-module approach using Make's **Converge** (available in paid plans) or simply duplicating modules.

---

## Step 6 — Module: Claude — Create a Message (Anthropic)

Add this module at the end of **each branch** (or after a Converge node):

1. **Module:** Anthropic → **Create a Message**
2. **Connection:** your Anthropic connection (API key)
3. **Model:** `claude-sonnet-4-6`
4. **Max tokens:** `2048`
5. **System prompt:** Copy the system prompt from `prompt/structuring-prompt.md`, replacing `{{current_date}}` with the Make expression:

   ```
   formatDate(now; "YYYY-MM-DD")
   ```

   In Make's text field, the system prompt becomes a static string with one dynamic injection. Use a **Set Variable** module to build it:

   - Variable name: `system_prompt`
   - Value: paste the full system prompt, and where `{{current_date}}` appears, use Make's `{{formatDate(now; "YYYY-MM-DD")}}`

6. **Messages → Role:** `user`
7. **Messages → Content:** 
   ```
   Source content:

   {{extracted_text}}
   ```
   Where `{{extracted_text}}` is mapped from the branch output (Whisper text, vision text, PDF text, or raw text).

8. Click **OK**

**Output:** The Claude response text is at `{{N.content[].text}}` — you may need a Text Aggregator to join array items into a single string.

---

## Step 7 — Module: Parse JSON

1. **Module:** **JSON** → **Parse JSON**
2. **JSON string:** map the Claude output text (the aggregated string from step 6)
3. Click **OK**

This gives you a Make array you can iterate over.

---

## Step 8 — Module: Iterator

1. **Module:** **Flow control** → **Iterator**
2. **Array:** map the parsed JSON array from step 7: `{{N.array}}`
3. Click **OK**

Each iteration outputs one task object with `title`, `notes`, `due`.

---

## Step 9 — Module: Create a Task (Google Tasks)

1. **Module:** **Google Tasks** → **Create a Task**
2. **Connection:** same Google connection (ensure Tasks scope is granted)
3. **Task List:** select your default task list (usually "My Tasks") or a dedicated "Brain Dump" list
4. **Title:** `{{iterator.title}}`
5. **Notes:** `{{iterator.notes}}`
6. **Due:** `{{iterator.due}}` — map as a date. If `due` is `null`, leave this blank. You may need a filter or `ifempty(iterator.due; "")`.
7. Click **OK**

---

## Step 10 — Module: Move the File to Actioned

Add this module **after** the iterator loop completes (i.e., after all tasks are created):

1. **Module:** **Google Drive** → **Move a File**
2. **Connection:** same Google connection
3. **File ID:** `{{1.id}}` (from the original trigger)
4. **Folder:** select `/Brain Dump/Actioned`
5. Click **OK**

> **Important:** This module must be **outside** the Iterator loop — it should run once per file, not once per task. In Make, place it after the Iterator's aggregate/output, not inside the iteration path.

---

## Step 11 — Error handling

1. Click the **wrench icon** on the Router module → **Add error handler**
2. Choose **Ignore** or **Break** depending on preference:
   - **Ignore:** silently skips the file (not recommended — failures stay invisible)
   - **Break:** stops the scenario and marks the run as failed — the file is NOT moved to Actioned because the Move module never runs. This is the correct behavior.
3. Optionally add a **Gmail → Send an Email** or **Slack → Send a Message** module in the error handler to notify you of failures.

For invalid JSON from Claude:
- Add a **JSON → Parse JSON** error handler that sends the raw Claude output to yourself by email so you can diagnose.

---

## Step 12 — Schedule / Trigger settings

1. Click the **clock icon** at the bottom of the scenario
2. **Scheduling:** set to **Immediately** (Make polls on your plan's minimum interval — 15 min on Free, 1 min on Core+)
3. Alternatively, use a **webhook**-based trigger if you want sub-minute response times (requires a small proxy, not covered here).
4. **Activate** the scenario using the toggle at the bottom.

---

## Step 13 — Test run

1. Click **Run once** to manually trigger
2. Drop a test `.txt` file into `/Brain Dump/To action` on Google Drive from your phone or desktop
3. Watch the execution log — each module should show green
4. Check Google Tasks for the created items
5. Verify the file moved to `/Brain Dump/Actioned`

---

## Mapping reference summary

| Module | Key field | Value |
|--------|-----------|-------|
| Watch Files | Folder | `/Brain Dump/To action` |
| Download File | File ID | `{{1.id}}` |
| Router branch filter | MIME type | regex per branch (see above) |
| Whisper | File | `{{2.data}}` |
| OpenAI vision | Image | `{{2.data}}` |
| Claude system | current_date | `formatDate(now; "YYYY-MM-DD")` |
| Claude user | extracted_text | branch text output |
| Iterator | Array | parsed JSON from Claude |
| Google Tasks title | — | `{{iterator.title}}` |
| Google Tasks notes | — | `{{iterator.notes}}` |
| Google Tasks due | — | `{{iterator.due}}` |
| Move File | File ID | `{{1.id}}` |
| Move File | Destination | `/Brain Dump/Actioned` |
