# Make.com — Manual Scenario Build Guide

Step-by-step instructions for building the Brain-Dump → Google Tasks scenario in the Make GUI.

> **Naming note:** In Make, the Anthropic/Claude module is called **"Create a prompt"** (under the Anthropic Claude app). Wherever this guide says "the Claude module", look for **Anthropic Claude → Create a prompt**.

---

## Prerequisites

- Make.com account (Free tier works for low volume; Core+ recommended for instant triggers)
- Google account with Drive and Tasks enabled
- AssemblyAI API key (for audio transcription — free tier: 100 hrs/month)
- Anthropic API key (for image reading, PDF reading, AND task structuring — Claude does all three)
- Google Drive folders already created:
  - `/Brain Dump/To action`
  - `/Brain Dump/Actioned`

> **No OpenAI needed.** Claude reads images and PDFs directly, so this build uses just two AI providers: **AssemblyAI** (audio) and **Anthropic Claude** (everything else).

---

## Step 1 — Add connections in Make

Do this before building the scenario so connections are ready to select as you go.

### Google
1. **Connections** → **Add a connection** → search **Google Drive** → sign in → grant all permissions. One connection covers both Drive and Tasks modules.
   - **Personal @gmail account?** Google blocks Make's default "restricted" Google connection on personal Gmail. You'll need a custom OAuth app: create a project in [Google Cloud Console](https://console.cloud.google.com), enable the Drive + Tasks APIs, create an OAuth consent screen (External, add your own email under **Test users**), create an OAuth client ID (Web application, redirect URI `https://www.integromat.com/oauth/cb/google-restricted`), then in Make choose **Advanced settings** and paste the Client ID + Secret. See README Part 1 for full detail.

### AssemblyAI
1. Sign up at [assemblyai.com](https://www.assemblyai.com) → copy your API key from the dashboard.
2. In Make: **Connections** → **Add** → search **AssemblyAI** → paste your API key → Save.

### Anthropic
1. **Connections** → **Add** → search **Anthropic** (or **Claude**) → paste your Anthropic API key → Save.

---

## Step 2 — Create a new scenario

1. Log in to Make.com → **Create a new scenario**
2. Name it: `Brain Dump → Google Tasks`
3. Leave the schedule as-is for now (you'll set it at the end)

---

## Step 3 — Module 1: Trigger — Watch Files in a Folder

1. Click the **+** button → search **Google Drive** → select **Watch Files in a Folder**
2. **Connection:** your Google connection
3. **Folder:** Click the folder picker → navigate to `/Brain Dump/To action`
4. **Watch:** New files only
5. **Maximum number of files:** `1` (process one file per run to keep things simple)
6. Click **OK**

> After saving, Make will ask which file to start watching from. Choose **"From now on"**.

---

## Step 4 — Module 2: Download a File

1. Click **+** after the trigger → **Google Drive** → **Download a File**
2. **Connection:** same Google connection
3. **File ID:** map from the trigger: `{{1.id}}`
4. Click **OK**

---

## Step 5 — Module 3: Router

1. Click **+** → search **Flow control** → select **Router**
2. The Router creates multiple branches. You will create **4 branches** (audio, image, PDF/doc, text).

Each branch normalises its input to plain text, then all branches feed into the Claude structuring step (Step 6).

### Branch 1 — Audio

1. Click the **+** on the first branch coming out of the Router
2. Set a **filter** on the link (click the wrench/spanner on the connector):
   - **Label:** `Audio`
   - **Condition:** `{{1.mimeType}}` **matches pattern (regex):** `^audio/`
   - OR fallback: `{{1.name}}` **matches pattern:** `\.(m4a|mp3|ogg|wav|aac)$`
3. **Module:** AssemblyAI → **Transcribe a Recording**
   - Connection: your AssemblyAI connection
   - Audio: map `{{2.data}}` (the downloaded bytes from Module 2)
   - Language code: leave blank for auto-detect, or set `en` for English
   - Leave all other options as default
4. **Output:** the transcript text is `{{3.text}}` (module 3's `text` field)

### Branch 2 — Image

1. Click **+** on the second Router branch
2. **Filter:**
   - **Label:** `Image`
   - **Condition:** `{{1.mimeType}}` **matches pattern:** `^image/`
   - OR: `{{1.name}}` **matches pattern:** `\.(jpg|jpeg|png|gif|webp|heic|heif)$`
3. **Module:** Anthropic Claude → **Create a prompt**
   - Connection: your Anthropic connection
   - Model: `claude-haiku-4-5` (cheap and good for OCR) or `claude-sonnet-4-6`
   - Max tokens: `1024`
   - Add a **user message**. If the module offers an **image / file input** (look for an "Add image", "Attachments", or "Content type: image" option):
     - Image data: map `{{2.data}}`
     - MIME type: `{{1.mimeType}}`
     - Text: `Extract and transcribe all text visible in this image. If there is no text, describe any actions or tasks implied. Return plain text only.`
   - **Output:** the text is `{{4.result}}` (or `{{4.content[].text}}` depending on the module version — pick the text field Make exposes)

> **If "Create a prompt" in your Make version is text-only** (no image input): use the **HTTP** module instead to call the Anthropic API directly with a base64 image block, OR skip image support for now and add it later. Most dumps are voice/text anyway.

### Branch 3 — PDF / Document

1. Click **+** on the third Router branch
2. **Filter:**
   - **Label:** `PDF or Document`
   - **Condition:** `{{1.mimeType}}` **matches pattern:** `^application/(pdf|msword|vnd\.openxmlformats|vnd\.ms)`
   - OR: `{{1.name}}` **matches pattern:** `\.(pdf|doc|docx)$`
3. **Module:** Anthropic Claude → **Create a prompt**
   - Connection: your Anthropic connection
   - Model: `claude-sonnet-4-6`
   - Max tokens: `2048`
   - Add a **user message**. If the module offers a **document / file input** (Claude reads PDFs natively):
     - Document data: map `{{2.data}}`
     - MIME type: `{{1.mimeType}}`
     - Text: `Extract all text from this document. Return plain text only — no commentary.`
   - **Output:** the text is `{{5.result}}` (or the text field Make exposes)

> **If your "Create a prompt" module can't accept a document/file:** add a dedicated extraction module first — **PDF.co** or **CloudConvert** (both have Make modules and free tiers) — to turn the PDF into text, then pass that text into Claude at Step 6. This avoids needing file input on the Claude module.

### Branch 4 — Plain Text

1. Click **+** on the fourth Router branch — set this as the **fallback** branch (no filter, or use Make's "fallback route" toggle)
2. **Filter label:** `Plain text (fallback)`
3. **Module:** **Tools** → **Set Variable**
   - Variable name: `extracted_text`
   - Value: `{{toString(2.data)}}` (decode the file bytes to a string)
   - If Make doesn't auto-decode, add **Tools → Base64 Decode** first, or a **Text Parser → Convert encoding** module.

---

## Step 6 — The Claude structuring step (Create a prompt)

This is the core step: normalised text → JSON task array. Add it **at the end of each branch** (the simplest reliable approach in Make — Router branches are independent paths, so each branch gets its own copy of the downstream modules).

> **Tip:** Build the full chain (Steps 6–10) on ONE branch first, test it end-to-end, then right-click the modules → **Clone** onto the other branches and just re-point the input mapping.

1. **Module:** Anthropic Claude → **Create a prompt**
2. **Connection:** your Anthropic connection
3. **Model:** `claude-sonnet-4-6`
4. **Max tokens:** `2048`
5. **System prompt:** Paste the system prompt from `prompt/structuring-prompt.md`. Where it says `{{current_date}}`, replace with Make's expression:
   ```
   {{formatDate(now; "YYYY-MM-DD")}}
   ```
6. **User message (Role: user):**
   ```
   Source content:

   {{<branch text output>}}
   ```
   Map `<branch text output>` to whichever this branch produced:
   - Audio branch → AssemblyAI `text`
   - Image branch → Claude image-read `result`/text
   - PDF branch → Claude PDF-read (or PDF.co) text
   - Text branch → the `extracted_text` variable
7. Click **OK**

**Output:** Claude's JSON response is in the module's result/text field (e.g. `{{N.result}}` or `{{N.content[].text}}`).

---

## Step 7 — Parse JSON

1. **Module:** **JSON** → **Parse JSON**
2. **JSON string:** map the Claude output text from Step 6
3. Click **OK**

This converts Claude's JSON string into a Make array you can iterate over.

---

## Step 8 — Iterator

1. **Module:** **Flow control** → **Iterator**
2. **Array:** map the parsed array from Step 7
3. Click **OK**

Each iteration outputs one task object with `title`, `notes`, `due`.

---

## Step 9 — Create a Task (Google Tasks)

1. **Module:** **Google Tasks** → **Create a Task**
2. **Connection:** same Google connection (Tasks scope granted)
3. **Task List:** select "My Tasks" or a dedicated "Brain Dump" list
4. **Title:** `{{iterator.title}}`
5. **Notes:** `{{iterator.notes}}`
6. **Due:** `{{iterator.due}}` — if `due` is `null`, leave blank. Use `{{ifempty(iterator.due; emptystring)}}` if Make complains about nulls.
7. Click **OK**

---

## Step 10 — Move the File to Actioned

Place this **after** the Iterator loop (runs once per file, not per task).

1. **Module:** **Google Drive** → **Move a File**
2. **Connection:** same Google connection
3. **File ID:** `{{1.id}}` (from the trigger)
4. **Folder:** select `/Brain Dump/Actioned`
5. Click **OK**

> **Important:** This must be **outside** the Iterator — after it, on the main flow, not inside the iteration path. This is also why failures stay visible: if any earlier module errors, Move never runs and the file stays in "To action".

---

## Step 11 — Error handling

1. Right-click a module that can fail (Claude, Parse JSON) → **Add error handler**
2. Choose **Break** (recommended):
   - Stops the run and marks it failed
   - The file is NOT moved to Actioned (Move never runs) → failure stays visible in "To action"
3. Optionally add **Gmail → Send an Email** inside the error handler to notify you, including the raw Claude output so you can diagnose bad JSON.

Common failure modes:
- **Empty/garbled extraction** → Claude returns `[]`, no tasks created. (Optionally add a filter before Move so empty results don't get archived.)
- **Invalid JSON** → Parse JSON errors → Break → file stays put.
- **Unsupported file type** → falls into the text branch; if binary, Claude returns `[]`. Add a `text/` filter on the fallback branch to be strict.

---

## Step 12 — Schedule / Trigger settings

1. Click the **clock icon** at the bottom of the scenario
2. **Scheduling:** **Immediately** (Make polls at your plan's minimum — 15 min Free, 1 min Core+)
3. **Activate** the scenario with the toggle.

---

## Step 13 — Test run

1. Click **Run once**
2. Drop a test `.txt` file into `/Brain Dump/To action`
3. Watch the execution log — each module should go green
4. Check Google Tasks for the created items
5. Verify the file moved to `/Brain Dump/Actioned`
6. Repeat for an audio file, an image, and a PDF.

---

## Mapping reference summary

| Module | Key field | Value |
|--------|-----------|-------|
| Watch Files | Folder | `/Brain Dump/To action` |
| Download File | File ID | `{{1.id}}` |
| Router filters | MIME type | regex per branch (see above) |
| AssemblyAI | Audio | `{{2.data}}` |
| Claude (image) | Image + MIME | `{{2.data}}` / `{{1.mimeType}}` |
| Claude (PDF) | Document + MIME | `{{2.data}}` / `{{1.mimeType}}` |
| Text branch | extracted_text | `{{toString(2.data)}}` |
| Claude system | current_date | `{{formatDate(now; "YYYY-MM-DD")}}` |
| Claude user | source content | branch text output |
| Iterator | Array | parsed JSON from Claude |
| Google Tasks | Title / Notes / Due | `{{iterator.title}}` / `{{iterator.notes}}` / `{{iterator.due}}` |
| Move File | File ID / Dest | `{{1.id}}` / `/Brain Dump/Actioned` |
