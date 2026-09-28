package com.codewithsam.mailautomator.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReferralRequestDto {

    @NotBlank(message = "companyName is required")
    private String companyName;

    @NotNull(message = "templateType is required (REFERRAL or INTERNAL_OPENING)")
    private TemplateType templateType;

    private String jobId;

    /** Optional. Must be a valid URL when provided. */
    private String jobLink;

    /** Optional. One or more locations, rendered as a "Location(s): ..." line in the referral template. */
    private List<String> locations;

    @NotBlank(message = "sheetId is required")
    private String sheetId;

    @NotBlank(message = "tabName is required")
    private String tabName;

    @AssertTrue(message = "jobLink must be a valid URL starting with http:// or https://")
    private boolean isJobLinkValid() {
        return jobLink == null || jobLink.isBlank() || jobLink.matches("^https?://.+");
    }
}
