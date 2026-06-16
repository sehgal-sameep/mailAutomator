package com.codewithsam.mailautomator.controller;

import com.codewithsam.mailautomator.dto.ReferralSummaryDto;
import com.codewithsam.mailautomator.manager.ReferralManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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
        summary = "Send referral emails",
        description = "Reads contacts from the configured Google Sheet, renders personalized emails, attaches resume and cover letter, and sends via Gmail SMTP.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Emails processed successfully",
                content = @Content(schema = @Schema(implementation = ReferralSummaryDto.class)))
        }
    )
    @PostMapping("/send")
    public ResponseEntity<ReferralSummaryDto> sendReferrals() {
        log.info("POST /referrals/send — received send request");
        ReferralSummaryDto summary = referralManager.orchestrateSendReferrals();
        return ResponseEntity.ok(summary);
    }
}
