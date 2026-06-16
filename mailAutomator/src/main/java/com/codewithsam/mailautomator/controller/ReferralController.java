package com.codewithsam.mailautomator.controller;

import com.codewithsam.mailautomator.dto.ReferralSummaryDto;
import com.codewithsam.mailautomator.manager.ReferralManager;
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
public class ReferralController {

    private final ReferralManager referralManager;

    /**
     * Triggers the referral email send process.
     *
     * <p>Reads contacts from the configured Google Sheet, renders personalized emails,
     * attaches resume and cover letter, and sends via Gmail API.
     *
     * @return summary of total/sent/skipped/failed counts
     */
    @PostMapping("/send")
    public ResponseEntity<ReferralSummaryDto> sendReferrals() {
        log.info("POST /referrals/send — received send request");
        ReferralSummaryDto summary = referralManager.orchestrateSendReferrals();
        return ResponseEntity.ok(summary);
    }
}
