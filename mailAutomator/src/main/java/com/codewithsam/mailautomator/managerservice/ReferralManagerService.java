package com.codewithsam.mailautomator.managerservice;

import com.codewithsam.mailautomator.dto.ManualReferralRequestDto;
import com.codewithsam.mailautomator.dto.ManualReferralSummaryDto;
import com.codewithsam.mailautomator.dto.ReferralRequestDto;
import com.codewithsam.mailautomator.dto.ReferralSummaryDto;

public interface ReferralManagerService {

    /**
     * Sheet-based flow: reads contacts from Google Sheets, then sends referral emails.
     */
    ReferralSummaryDto sendReferralEmails(ReferralRequestDto request);

    /**
     * Manual flow: sends referral emails to recipients supplied directly in the request.
     */
    ManualReferralSummaryDto sendReferralEmailsManual(ManualReferralRequestDto request);
}
