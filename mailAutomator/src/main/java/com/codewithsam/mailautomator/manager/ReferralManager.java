package com.codewithsam.mailautomator.manager;

import com.codewithsam.mailautomator.dto.ReferralSummaryDto;
import com.codewithsam.mailautomator.managerservice.ReferralManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReferralManager {

    private final ReferralManagerService referralManagerService;

    /**
     * Entry point for the referral send flow.
     * Delegates business coordination to {@link ReferralManagerService}.
     */
    public ReferralSummaryDto orchestrateSendReferrals() {
        log.info("ReferralManager: initiating referral send flow");
        ReferralSummaryDto summary = referralManagerService.sendReferralEmails();
        log.info("ReferralManager: flow complete — {}", summary);
        return summary;
    }
}
