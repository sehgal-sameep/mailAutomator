# Mail Automator

Automated referral email sender. Sends personalized referral request emails
(with resume + cover letter attachments) via Gmail SMTP.

Supports two modes:

- **Google Sheet mode** — reads recipient details from a Google Sheet tab
- **Manual mode** — accepts recipient details directly in the API request body (no sheet required)

No restart needed to target a different company or job — all details are passed dynamically via the API payload.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4.x (Java 17) |
| Google Sheets | Google Sheets API v4 |
| Email sending | Gmail SMTP (App Password) |
| Auth | Google Service Account (JSON key) |
| Build | Maven |
| Utilities | Lombok, springdoc-openapi |

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
| `config` | Static config properties + Google API beans |
| `util` | Email validation, template rendering |
| `dto` | Data transfer objects — request/response DTOs for both send modes |
| `exception` | Custom exceptions + global handler |

---

## What comes from the API payload vs application.yml

### Passed per-request (API payload)

These values change per job opening and are sent in the request body — no restart required:

| Field | Description |
|---|---|
| `companyName` | Company name used in email subject and template |
| `jobId` | Job posting ID used in email subject and template |
| `jobLink` | Full URL to the job posting used in template |
| `sheetId` | Google Sheet spreadsheet ID |
| `tabName` | Sheet tab name containing the contacts |

### Static config (application.yml / env vars)

These are system-level settings that stay fixed across all requests:

| Property / Env Var | Description |
|---|---|
| `GMAIL_SENDER_EMAIL` / `spring.mail.username` | Gmail address used to send emails |
| `GMAIL_APP_PASSWORD` / `spring.mail.password` | Gmail App Password |
| `GOOGLE_SERVICE_ACCOUNT_KEY_PATH` | Path to GCP service account JSON key |
| `EMAIL_TEMPLATE_PATH` | Path to the `.txt` email template |
| `RESUME_PATH` | Path to resume PDF attachment |
| `COVER_LETTER_PATH` | Path to cover letter PDF attachment |
| `DRY_RUN` | `true` to log recipients without sending |

---

## Google Sheet Format

The sheet tab name is passed in the request (`tabName`). Convention: `<companyName>_emails`

Example: `acmecorp_emails`

| S No | First Name | Last Name | Company Email | Personal Email | Designation |
|---|---|---|---|---|---|
| 1 | Alice | Johnson | alice.johnson@acmecorp.com | alice.j@gmail.com | |
| 2 | Bob | Smith | bob.smith@acmecorp.com | | Talent Advisor |
| 3 | Carol | Williams | carol.williams@acmecorp.com | carol.w@gmail.com | TA |

- **Column A** – Serial number
- **Column B** – First name
- **Column C** – Last name
- **Column D** – Company email
- **Column E** – Personal email

### Email sending behavior per row

- If **only company email** is present → one email sent to company email
- If **only personal email** is present → one email sent to personal email
- If **both** are present → **two separate emails sent**, one to each address
- If both are the same address → only one email sent (deduplication)
- If both are missing or invalid → row is skipped with a warning

The same deduplication applies globally across all rows: if the same address appears in multiple rows, it receives only one email per run.

Empty rows are skipped automatically.

---

## API Reference

### POST `/referrals/send` — Google Sheet mode

Reads contacts from the given Google Sheet tab and sends personalized referral emails.

**Request payload — REFERRAL:**

```json
{
  "companyName": "AcmeCorp",
  "templateType": "REFERRAL",
  "jobId": "JR-123456",
  "jobLink": "https://careers.acmecorp.com/job/123456",
  "sheetId": "your-google-spreadsheet-id",
  "tabName": "acmecorp_emails"
}
```

**Request payload — INTERNAL_OPENING:**

```json
{
  "companyName": "AcmeCorp",
  "templateType": "INTERNAL_OPENING",
  "sheetId": "your-google-spreadsheet-id",
  "tabName": "acmecorp_emails"
}
```

**Field validation:**

| Field | Rule |
|---|---|
| `companyName` | Required, not blank |
| `templateType` | Required — `REFERRAL` or `INTERNAL_OPENING` |
| `jobId` | Required when `templateType` is `REFERRAL`; ignored for `INTERNAL_OPENING` |
| `jobLink` | Required when `templateType` is `REFERRAL`; must be a valid URL; ignored for `INTERNAL_OPENING` |
| `sheetId` | Required, not blank |
| `tabName` | Required, not blank |

**Successful response (200):**

```json
{
  "totalRecords": 7,
  "totalRecipients": 11,
  "emailsSent": 10,
  "rowsSkipped": 1,
  "duplicatesSkipped": 0,
  "failedEmails": []
}
```

| Response field | Meaning |
|---|---|
| `totalRecords` | Rows read from the Google Sheet (excluding header) |
| `totalRecipients` | Total valid email addresses found across all contacts |
| `emailsSent` | Emails successfully sent |
| `rowsSkipped` | Contacts skipped because no valid email was found |
| `duplicatesSkipped` | Addresses skipped because already sent to in this run |
| `failedEmails` | Addresses that threw an error during sending |

---

### POST `/referrals/send/manual` — Manual mode

Sends referral emails to recipients supplied directly in the request body. No Google Sheet required — useful for quick, small batches.

**Request payload:**

```json
{
  "companyName": "Mastercard",
  "jobId": "R-123456",
  "jobLink": "https://careers.mastercard.com/job/example",
  "recipients": [
    {
      "firstName": "Vidushi",
      "lastName": "Shukla",
      "emails": [
        "vidushi.shukla@mastercard.com",
        "vidushishukla005@gmail.com"
      ]
    },
    {
      "firstName": "Khushboo",
      "emails": [
        "khushboo.munjal@mastercard.com"
      ]
    }
  ]
}
```

**Field validation:**

| Field | Rule |
|---|---|
| `companyName` | Required, not blank |
| `jobId` | Required, not blank |
| `jobLink` | Required; must be a valid URL starting with `http://` or `https://` |
| `recipients` | Required; must not be empty |
| `recipients[].firstName` | Required, not blank |
| `recipients[].lastName` | Optional |
| `recipients[].emails` | Required; must contain at least one entry |

Individual email addresses with invalid format are silently skipped (counted in `emailsSkipped`). Recipients where none of the supplied addresses are valid are also skipped.

**Successful response (200):**

```json
{
  "totalRecipients": 2,
  "totalEmailAddresses": 3,
  "emailsSent": 3,
  "emailsSkipped": 0,
  "duplicateEmailsSkipped": 0,
  "failedEmails": []
}
```

| Response field | Meaning |
|---|---|
| `totalRecipients` | Number of recipient objects in the request |
| `totalEmailAddresses` | Total valid email addresses across all recipients |
| `emailsSent` | Emails successfully sent |
| `emailsSkipped` | Recipients skipped because no valid email address was found |
| `duplicateEmailsSkipped` | Addresses skipped because already sent to in this run |
| `failedEmails` | Addresses that threw an error during sending |

**Validation error response (400):**

```json
{
  "error": "Validation failed",
  "fieldErrors": {
    "jobLink": "jobLink must be a valid URL starting with http:// or https://"
  }
}
```

**Swagger UI:** `http://localhost:8082/swagger-ui/index.html`

---

## Sending emails for different companies without restarting

Just change the payload — the service keeps running.

**Sheet mode — AcmeCorp:**
```bash
curl -X POST http://localhost:8082/referrals/send \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "AcmeCorp",
    "templateType": "REFERRAL",
    "jobId": "JR-100",
    "jobLink": "https://careers.acmecorp.com/job/100",
    "sheetId": "sheet-id-for-acmecorp",
    "tabName": "acmecorp_emails"
  }'
```

**Manual mode — quick batch without a sheet:**
```bash
curl -X POST http://localhost:8082/referrals/send/manual \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "TechCorp",
    "jobId": "TC-999",
    "jobLink": "https://jobs.techcorp.io/999",
    "recipients": [
      {
        "firstName": "Alice",
        "lastName": "Johnson",
        "emails": ["alice.johnson@techcorp.io", "alice.j@gmail.com"]
      },
      {
        "firstName": "Bob",
        "emails": ["bob.smith@techcorp.io"]
      }
    ]
  }'
```

---

## Gmail & Google API Setup

### Step 1 — Enable APIs

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project (or select an existing one)
3. Navigate to **APIs & Services → Library**
4. Enable **Google Sheets API**

### Step 2 — Create a Service Account Key

1. Go to **IAM & Admin → Service Accounts**
2. Create a service account (or select existing)
3. Click **Keys → Add Key → Create new key → JSON**
4. Save the downloaded file as `google-service-account.json`
5. Place it in the `mailAutomator/` directory (next to `pom.xml`)
6. Set `GOOGLE_SERVICE_ACCOUNT_KEY_PATH` if using a different path

### Step 3 — Share each Google Sheet with the service account

Open the sheet → **Share** → paste the service account email
(found in the JSON key as `client_email`) → set role to **Viewer**.

### Step 4 — Generate a Gmail App Password

1. Enable 2-Step Verification on your Google account
2. Go to `myaccount.google.com/apppasswords`
3. Generate a password and set it as `GMAIL_APP_PASSWORD`

---

## Setup Steps

```bash
# 1. Clone / open the project
cd mailAutomator

# 2. Place google-service-account.json next to pom.xml

# 3. Place resume and cover letter files (paths configured in application.yml)

# 4. Set required environment variables
export GMAIL_SENDER_EMAIL=you@gmail.com
export GMAIL_APP_PASSWORD=your-app-password
export GOOGLE_SERVICE_ACCOUNT_KEY_PATH=google-service-account.json

# 5. Build
./mvnw clean package

# 6. Run
./mvnw spring-boot:run
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

## Email Templates

The template used is selected automatically based on `templateType` in the request.

| `templateType` | Template file | Email subject |
|---|---|---|
| `REFERRAL` | `templates/referral-email-template.txt` | `Referral Request - <companyName> \| Job ID: <jobId>` |
| `INTERNAL_OPENING` | `templates/internal-opening-email-template.txt` | `Internal Openings Enquiry - <companyName>` |

**Supported placeholders (both templates):**

| Placeholder | Value |
|---|---|
| `{{firstName}}` | Contact's first name |
| `{{lastName}}` | Contact's last name |
| `{{companyName}}` | From request payload |
| `{{jobId}}` | From request payload — blank for `INTERNAL_OPENING` |
| `{{jobLink}}` | From request payload — blank for `INTERNAL_OPENING` |

To override template paths, set `REFERRAL_TEMPLATE_PATH` or `INTERNAL_OPENING_TEMPLATE_PATH` to any filesystem path.

---

## Common Troubleshooting

| Problem | Fix |
|---|---|
| `403 Forbidden` from Sheets API | Share the Google Sheet with the service account `client_email` |
| `Unable to parse range: <tabName>!A:E` | The tab name in `tabName` doesn't match any tab in the sheet |
| `Google service account key not found` | Check `GOOGLE_SERVICE_ACCOUNT_KEY_PATH` points to the correct file |
| `400 Validation failed` | Check the API response `fieldErrors` for which field is invalid |
| Email skipped with "no valid email" | The row has both company and personal email blank — add at least one |
| Attachment not included | Check that `RESUME_PATH` / `COVER_LETTER_PATH` point to existing files |
| `403 Forbidden` from Gmail SMTP | Check `GMAIL_APP_PASSWORD` is correct and 2-Step Verification is on |
