package com.codewithsam.mailautomator.managerservice;

import com.codewithsam.mailautomator.dto.ReferralSummaryDto;

public interface ReferralManagerService {

    /**
     * Coordinates the full referral send flow:
     * reads contacts → validates → renders templates → sends emails → returns summary.
     */
    ReferralSummaryDto sendReferralEmails();
}
