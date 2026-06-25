package com.codewithsam.mailautomator.manager;

import com.codewithsam.mailautomator.dto.ManualReferralRequestDto;
import com.codewithsam.mailautomator.dto.ManualReferralSummaryDto;
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
        log.info("ReferralManager: initiating sheet-based referral send flow for company={}", request.getCompanyName());
        ReferralSummaryDto summary = referralManagerService.sendReferralEmails(request);
        log.info("ReferralManager: sheet-based flow complete — {}", summary);
        return summary;
    }

    public ManualReferralSummaryDto orchestrateSendReferralsManual(ManualReferralRequestDto request) {
        log.info("ReferralManager: initiating manual referral send flow for company={}", request.getCompanyName());
        ManualReferralSummaryDto summary = referralManagerService.sendReferralEmailsManual(request);
        log.info("ReferralManager: manual flow complete — {}", summary);
        return summary;
    }
}
