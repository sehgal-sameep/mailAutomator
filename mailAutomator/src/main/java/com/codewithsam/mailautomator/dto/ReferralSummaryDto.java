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
public class ReferralSummaryDto {

    /** Total rows read from the Google Sheet (excluding header). */
    private int totalRecords;

    /** Total valid email addresses found across all contacts. */
    private int totalRecipients;

    /** Emails sent successfully. */
    private int emailsSent;

    /** Contacts skipped because no valid email address was found. */
    private int rowsSkipped;

    /** Email addresses skipped because already sent to in this run. */
    private int duplicatesSkipped;

    /** Email addresses that failed to send. */
    private List<String> failedEmails;
}
