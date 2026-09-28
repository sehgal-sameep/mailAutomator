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

            **Optional:** `jobId`, `jobLink` (must start with `http://` or `https://`), `locations` (array of one or more strings) — each is rendered as its own line only when provided (one location → `Location: X`, several → `Locations: X, Y`), and only in the `REFERRAL` template (`INTERNAL_OPENING` ignores them). `jobId` is also appended to the subject.

            **Examples**

            Minimal:
            ```json
            { "companyName": "Acme", "templateType": "REFERRAL", "sheetId": "<sheet-id>", "tabName": "Sheet1" }
            ```
            With job ID only:
            ```json
            { "companyName": "Acme", "templateType": "REFERRAL", "jobId": "12345", "sheetId": "<sheet-id>", "tabName": "Sheet1" }
            ```
            All job details:
            ```json
            { "companyName": "Acme", "templateType": "REFERRAL", "jobId": "12345", "jobLink": "https://acme.com/jobs/12345", "locations": ["Bangalore", "Pune"], "sheetId": "<sheet-id>", "tabName": "Sheet1" }
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
        log.info("POST /referrals/send — company={}, jobId={}", request.getCompanyName(), request.getJobId());
        ReferralSummaryDto summary = referralManager.orchestrateSendReferrals(request);
        return ResponseEntity.ok(summary);
    }

    @Operation(
        summary = "Send referral emails (manual recipient list)",
        description = """
            Sends personalized referral emails to recipients supplied directly in the request body — no Google Sheet required. Useful for quick, small batches. Always uses the `REFERRAL` template.

            **Mandatory:** `companyName`, `recipients` (non-empty; each needs `firstName` and at least one entry in `emails`)

            **Optional:** `jobId`, `jobLink` (must start with `http://` or `https://`), `locations` (array of one or more strings), and per-recipient `lastName`. Job fields are rendered as their own lines only when provided (one location → `Location: X`, several → `Locations: X, Y`); `jobId` is also appended to the subject.

            **Examples**

            Minimal:
            ```json
            { "companyName": "Acme", "recipients": [ { "firstName": "Priya", "emails": ["priya@acme.com"] } ] }
            ```
            With job link and a single location:
            ```json
            { "companyName": "Acme", "jobLink": "https://acme.com/jobs/12345", "locations": ["Bangalore"], "recipients": [ { "firstName": "Priya", "emails": ["priya@acme.com"] } ] }
            ```
            All fields, multiple recipients:
            ```json
            { "companyName": "Acme", "jobId": "12345", "jobLink": "https://acme.com/jobs/12345", "locations": ["Bangalore", "Pune"], "recipients": [ { "firstName": "Priya", "lastName": "Sharma", "emails": ["priya@acme.com", "priya.s@gmail.com"] }, { "firstName": "Rahul", "emails": ["rahul@acme.com"] } ] }
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
        log.info("POST /referrals/send/manual — company={}, jobId={}, recipients={}",
                request.getCompanyName(), request.getJobId(), request.getRecipients().size());
        ManualReferralSummaryDto summary = referralManager.orchestrateSendReferralsManual(request);
        return ResponseEntity.ok(summary);
    }
}
