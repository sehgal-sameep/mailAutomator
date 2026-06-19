package com.codewithsam.mailautomator.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ReferralRequestDto {

    @NotBlank(message = "companyName is required")
    private String companyName;

    @NotNull(message = "templateType is required (REFERRAL or INTERNAL_OPENING)")
    private TemplateType templateType;

    /** Required only when templateType is REFERRAL. */
    private String jobId;

    /** Required only when templateType is REFERRAL. Must be a valid URL when provided. */
    @Pattern(regexp = "^https?://.+", message = "jobLink must be a valid URL starting with http:// or https://")
    private String jobLink;

    @NotBlank(message = "sheetId is required")
    private String sheetId;

    @NotBlank(message = "tabName is required")
    private String tabName;

    @AssertTrue(message = "jobId and jobLink are required when templateType is REFERRAL")
    private boolean isJobDetailsValidForTemplateType() {
        if (TemplateType.REFERRAL.equals(templateType)) {
            return jobId != null && !jobId.isBlank()
                    && jobLink != null && !jobLink.isBlank();
        }
        return true;
    }
}
