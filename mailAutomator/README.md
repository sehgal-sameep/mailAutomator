# Mail Automator

Automated referral email sender. Reads candidate details from a Google Sheet and
sends personalized referral request emails (with resume + cover letter attachments)
via the Gmail API.

Designed for one trigger per job opening — just update the job details in
`application.yml` (or env vars) and call the API.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4.x (Java 17) |
| Google Sheets | Google Sheets API v4 |
| Email sending | Gmail API v1 |
| Auth | Google OAuth 2.0 (installed app flow) |
| Build | Maven |
| Utilities | Lombok |

---

## Project Architecture

```
Controller → Manager → ManagerService → Service → ServiceImpl
```

| Package | Responsibility |
|---|---|
| `controller` | Exposes REST API, handles HTTP in/out |
| `manager` | Orchestration entry point |
| `managerservice` | Business coordination (contacts → template → send) |
| `service` | Core service contracts (interfaces) |
| `serviceimpl` | Actual implementations |
| `config` | External config properties + Google API beans |
| `util` | Email validation, template rendering |
| `dto` | Data transfer objects |
| `exception` | Custom exceptions + global handler |

---

## Google Sheet Format

The sheet **tab name** must follow the pattern: `<companyName>_emails`

Example: `acmecorp_emails`

| S No | First Name | Last Name | Company Email | Personal Email | Designation |
|---|---|---|---|---|---|
| 1 | Alice | Johnson | alice.johnson@acmecorp.com | alice.j@gmail.com | |
| 2 | Bob | Smith | bob.smith@acmecorp.com | | Talent Advisor |
| 3 | Carol | Williams | carol.williams@acmecorp.com | carol.w@gmail.com | TA |

- **Column A** – Serial number
- **Column B** – First name
- **Column C** – Last name
- **Column D** – Company email *(used as primary)*
- **Column E** – Personal email *(fallback if company email is blank)*
- **Column F** – Designation *(optional)*

Empty rows are skipped automatically. Rows with no valid email are skipped with a warning.

---

## Required Environment Variables

| Variable | Description | Example |
|---|---|---|
| `COMPANY_NAME` | Company name shown in email and sheet name | `AcmeCorp` |
| `JOB_ID` | Job posting ID | `JR-123456` |
| `JOB_LINK` | Full URL to the job posting | `https://careers.acmecorp.com/job/123456` |
| `GOOGLE_SHEET_ID` | Spreadsheet ID from the Google Sheet URL | `1BxiM...` |
| `GOOGLE_SHEET_NAME` | Sheet tab name (defaults to `<COMPANY_NAME>_emails`) | `acmecorp_emails` |
| `GMAIL_SENDER_EMAIL` | Your Gmail address | `you@gmail.com` |
| `GMAIL_CREDENTIALS_PATH` | Path to `credentials.json` | `credentials.json` |
| `GMAIL_TOKENS_DIR` | Directory to store OAuth tokens | `tokens` |
| `EMAIL_TEMPLATE_PATH` | Path to `.txt` email template | `templates/referral-email-template.txt` |
| `RESUME_PATH` | Path to resume PDF | `files/resume.pdf` |
| `COVER_LETTER_PATH` | Path to cover letter PDF | `files/cover-letter.pdf` |
| `DRY_RUN` | `true` to preview without sending | `false` |

> You can override any of these in `application.yml` directly for local development
> (just avoid committing actual credentials).

---

## Gmail & Google API Setup

### Step 1 — Enable APIs

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project (or select an existing one)
3. Navigate to **APIs & Services → Library**
4. Enable **Google Sheets API**
5. Enable **Gmail API**

### Step 2 — Create OAuth2 Credentials

1. Go to **APIs & Services → Credentials**
2. Click **Create Credentials → OAuth 2.0 Client ID**
3. Set Application type to **Desktop app**
4. Download the JSON file and rename it to `credentials.json`
5. Place it in your project root (same directory as `pom.xml`)

### Step 3 — First-Run Authorization

On first startup, a browser window will open for you to log in and grant
access to Sheets (read) and Gmail (send). Tokens are stored in the `tokens/`
directory. All subsequent runs use the stored token automatically.

> If you get a "This app isn't verified" warning, click **Advanced → Go to app (unsafe)** — this is your own app.

### Step 4 — Share the Google Sheet

Share your Google Sheet with the Gmail account you authorized (or keep it
accessible to anyone with the link).

---

## Setup Steps

```bash
# 1. Clone / open the project
cd mailAutomator

# 2. Place your credentials.json in the project root
# 3. Place resume.pdf and cover-letter.pdf in src/main/resources/files/

# 4. Set environment variables (or edit application.yml directly for local runs)
export COMPANY_NAME=AcmeCorp
export JOB_ID=JR-123456
export JOB_LINK=https://careers.acmecorp.com/job/123456
export GOOGLE_SHEET_ID=your-spreadsheet-id
export GMAIL_SENDER_EMAIL=you@gmail.com

# 5. Build
./mvnw clean package

# 6. Run
./mvnw spring-boot:run
```

---

## How to Trigger the Referral Email API

```bash
curl -X POST http://localhost:8082/referrals/send
```

**Response:**

```json
{
  "totalRecords": 7,
  "emailsSent": 6,
  "emailsSkipped": 1,
  "failedEmails": []
}
```

---

## Dry-Run Mode

Dry-run logs who would receive an email **without actually sending anything**.

Enable it in `application.yml`:
```yaml
email:
  dry-run: true
```

Or via environment variable:
```bash
export DRY_RUN=true
```

You will see log lines like:
```
[DRY RUN] Would send to: Alice Johnson <alice.johnson@acmecorp.com>
```

---

## Email Template

The default template lives at `src/main/resources/templates/referral-email-template.txt`.

Supported placeholders:

| Placeholder | Value |
|---|---|
| `{{firstName}}` | Contact's first name |
| `{{lastName}}` | Contact's last name |
| `{{companyName}}` | From `job.company-name` |
| `{{jobId}}` | From `job.job-id` |
| `{{jobLink}}` | From `job.job-link` |

To use a custom template, set `email.template-path` to any file path on your
filesystem (absolute or relative to the working directory).

---

## Switching to a New Job Opening

Just update these three values and call the API again:

```yaml
job:
  company-name: AcmeCorp
  job-id: ${JOB_ID}
  job-link: ${JOB_LINK}

google:
  sheets:
    sheet-name: acmecorp_emails
```

Or export the corresponding env vars and restart the app.

---

## Common Troubleshooting

| Problem | Fix |
|---|---|
| `credentials.json not found` | Make sure the file is in the path set by `GMAIL_CREDENTIALS_PATH` |
| Browser doesn't open for auth | Run the app locally; headless servers cannot complete the OAuth flow |
| `Token has been expired or revoked` | Delete the `tokens/` directory and re-authorize |
| `Google Sheet not found` | Verify `GOOGLE_SHEET_ID` and `GOOGLE_SHEET_NAME` match exactly |
| Email skipped with "no valid email" | The row has both company and personal email blank — add at least one |
| Attachment not included | Check that `RESUME_PATH` / `COVER_LETTER_PATH` point to existing files |
| `403 Forbidden` from Gmail API | Re-check that the Gmail API is enabled in your Google Cloud project |
| Port 8888 already in use during auth | Change the `LocalServerReceiver` port in `GoogleApiConfig.java` |
