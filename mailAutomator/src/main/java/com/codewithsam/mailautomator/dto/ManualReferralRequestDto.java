package com.codewithsam.mailautomator.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ManualReferralRequestDto {

    @NotBlank(message = "companyName is required")
    private String companyName;

    private String jobId;

    private String jobLink;

    @NotEmpty(message = "recipients must not be empty")
    @Valid
    private List<RecipientDto> recipients;

    @AssertTrue(message = "jobLink must be a valid URL starting with http:// or https://")
    private boolean isJobLinkValid() {
        return jobLink == null || jobLink.isBlank() || jobLink.matches("^https?://.+");
    }
}
