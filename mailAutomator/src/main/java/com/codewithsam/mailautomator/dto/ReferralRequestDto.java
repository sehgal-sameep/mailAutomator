package com.codewithsam.mailautomator.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReferralRequestDto {

    @NotBlank(message = "companyName is required")
    private String companyName;

    @NotNull(message = "templateType is required (REFERRAL or INTERNAL_OPENING)")
    private TemplateType templateType;

    private String jobId;

    /** Optional. Must be a valid URL when provided. */
    private String jobLink;

    @NotBlank(message = "sheetId is required")
    private String sheetId;

    @NotBlank(message = "tabName is required")
    private String tabName;

    @AssertTrue(message = "jobLink must be a valid URL starting with http:// or https://")
    private boolean isJobLinkValid() {
        return jobLink == null || jobLink.isBlank() || jobLink.matches("^https?://.+");
    }
}
