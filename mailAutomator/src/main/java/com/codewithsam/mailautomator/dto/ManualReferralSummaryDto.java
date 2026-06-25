package com.codewithsam.mailautomator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualReferralSummaryDto {

    /** Number of recipient objects in the request. */
    private int totalRecipients;

    /** Total email addresses across all recipients. */
    private int totalEmailAddresses;

    /** Emails sent successfully. */
    private int emailsSent;

    /** Recipients skipped because no valid email address was found. */
    private int emailsSkipped;

    /** Email addresses skipped because already sent to in this run. */
    private int duplicateEmailsSkipped;

    /** Email addresses that failed to send. */
    private List<String> failedEmails;
}
