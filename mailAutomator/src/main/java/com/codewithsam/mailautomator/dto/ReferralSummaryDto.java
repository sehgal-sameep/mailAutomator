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

    private int totalRecords;
    private int emailsSent;
    private int emailsSkipped;
    private List<String> failedEmails;
}
