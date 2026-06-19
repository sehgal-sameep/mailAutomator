package com.codewithsam.mailautomator.manager;

import com.codewithsam.mailautomator.dto.ReferralRequestDto;
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

    public ReferralSummaryDto orchestrateSendReferrals(ReferralRequestDto request) {
        log.info("ReferralManager: initiating referral send flow for company={}", request.getCompanyName());
        ReferralSummaryDto summary = referralManagerService.sendReferralEmails(request);
        log.info("ReferralManager: flow complete — {}", summary);
        return summary;
    }
}
