package com.codewithsam.mailautomator.controller;

import com.codewithsam.mailautomator.dto.ManualReferralRequestDto;
import com.codewithsam.mailautomator.dto.ManualReferralSummaryDto;
import com.codewithsam.mailautomator.dto.ReferralRequestDto;
import com.codewithsam.mailautomator.dto.ReferralSummaryDto;
import com.codewithsam.mailautomator.manager.ReferralManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/referrals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Referrals", description = "Trigger and monitor referral email campaigns")
public class ReferralController {

    private final ReferralManager referralManager;

    @Operation(
        summary = "Send referral emails (Google Sheet source)",
        description = """
            Reads contacts from the given Google Sheet tab, renders personalized emails using the provided job details, and sends via Gmail SMTP. No restart needed to target a different company or job.

            **Mandatory:** `companyName`, `templateType` (`REFERRAL` | `INTERNAL_OPENING`), `sheetId`, `tabName`

            **Optional:** `jobs` — array of openings, each with optional `roleTitle`, `jobId`, `jobLink` (must start with `http://` or `https://`) and `locations` (array of strings). All openings go into a single email: one opening → the intro names its role and its details follow as lines; several → a numbered list, one block per opening. Fields render only when provided (one location → `Location: X`, several → `Locations: X, Y`), and only in the `REFERRAL` template (`INTERNAL_OPENING` ignores them). Job IDs are appended to the subject.

            **Examples**

            Minimal:
            ```json
            { "companyName": "Acme", "templateType": "REFERRAL", "sheetId": "<sheet-id>", "tabName": "Sheet1" }
            ```
            Single opening:
            ```json
            { "companyName": "Acme", "templateType": "REFERRAL", "jobs": [ { "roleTitle": "Backend Engineer", "jobId": "12345" } ], "sheetId": "<sheet-id>", "tabName": "Sheet1" }
            ```
            Multiple openings:
            ```json
            { "companyName": "Acme", "templateType": "REFERRAL", "jobs": [ { "roleTitle": "Backend Engineer", "jobId": "12345", "jobLink": "https://acme.com/jobs/12345", "locations": ["Bangalore", "Pune"] }, { "roleTitle": "Software Engineer II", "jobId": "67890", "jobLink": "https://acme.com/jobs/67890" } ], "sheetId": "<sheet-id>", "tabName": "Sheet1" }
            ```
            Internal openings enquiry:
            ```json
            { "companyName": "Acme", "templateType": "INTERNAL_OPENING", "sheetId": "<sheet-id>", "tabName": "Sheet1" }
            ```
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Emails processed successfully",
                content = @Content(schema = @Schema(implementation = ReferralSummaryDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload — see fieldErrors in response body")
        }
    )
    @PostMapping("/send")
    public ResponseEntity<ReferralSummaryDto> sendReferrals(@RequestBody @Valid ReferralRequestDto request) {
        log.info("POST /referrals/send — company={}, jobs={}", request.getCompanyName(),
                request.getJobs() == null ? 0 : request.getJobs().size());
        ReferralSummaryDto summary = referralManager.orchestrateSendReferrals(request);
        return ResponseEntity.ok(summary);
    }

    @Operation(
        summary = "Send referral emails (manual recipient list)",
        description = """
            Sends personalized referral emails to recipients supplied directly in the request body — no Google Sheet required. Useful for quick, small batches. Always uses the `REFERRAL` template.

            **Mandatory:** `companyName`, `recipients` (non-empty; each needs `firstName` and at least one entry in `emails`)

            **Optional:** `jobs` — array of openings, each with optional `roleTitle`, `jobId`, `jobLink` (must start with `http://` or `https://`) and `locations` (array of strings) — and per-recipient `lastName`. All openings go into a single email (several → a numbered list); fields render only when provided. Job IDs are appended to the subject.

            **Examples**

            Minimal:
            ```json
            { "companyName": "Acme", "recipients": [ { "firstName": "Priya", "emails": ["priya@acme.com"] } ] }
            ```
            Single opening:
            ```json
            { "companyName": "Acme", "jobs": [ { "roleTitle": "Backend Engineer", "jobLink": "https://acme.com/jobs/12345", "locations": ["Bangalore"] } ], "recipients": [ { "firstName": "Priya", "emails": ["priya@acme.com"] } ] }
            ```
            Multiple openings, multiple recipients:
            ```json
            { "companyName": "Acme", "jobs": [ { "roleTitle": "Backend Engineer", "jobId": "12345", "jobLink": "https://acme.com/jobs/12345", "locations": ["Bangalore", "Pune"] }, { "roleTitle": "Software Engineer II", "jobId": "67890" } ], "recipients": [ { "firstName": "Priya", "lastName": "Sharma", "emails": ["priya@acme.com", "priya.s@gmail.com"] }, { "firstName": "Rahul", "emails": ["rahul@acme.com"] } ] }
            ```
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Emails processed successfully",
                content = @Content(schema = @Schema(implementation = ManualReferralSummaryDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload — see fieldErrors in response body")
        }
    )
    @PostMapping("/send/manual")
    public ResponseEntity<ManualReferralSummaryDto> sendReferralsManual(@RequestBody @Valid ManualReferralRequestDto request) {
        log.info("POST /referrals/send/manual — company={}, jobs={}, recipients={}",
                request.getCompanyName(), request.getJobs() == null ? 0 : request.getJobs().size(),
                request.getRecipients().size());
        ManualReferralSummaryDto summary = referralManager.orchestrateSendReferralsManual(request);
        return ResponseEntity.ok(summary);
    }
}
