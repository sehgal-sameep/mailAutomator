package com.codewithsam.mailautomator.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class ManualReferralRequestDto {

    @NotBlank(message = "companyName is required")
    private String companyName;

    @NotBlank(message = "jobId is required")
    private String jobId;

    @NotBlank(message = "jobLink is required")
    @Pattern(regexp = "^https?://.+", message = "jobLink must be a valid URL starting with http:// or https://")
    private String jobLink;

    @NotEmpty(message = "recipients must not be empty")
    @Valid
    private List<RecipientDto> recipients;
}
