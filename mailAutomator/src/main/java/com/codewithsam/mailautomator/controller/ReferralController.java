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
        description = "Reads contacts from the given Google Sheet tab, renders personalized emails using the provided job details, and sends via Gmail SMTP. No restart needed to target a different company or job.",
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
        description = "Sends personalized referral emails to recipients supplied directly in the request body — no Google Sheet required. Useful for quick, small batches.",
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
