package com.codewithsam.mailautomator.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ManualReferralRequestDto {

    @NotBlank(message = "companyName is required")
    private String companyName;

    /** Optional. One or more openings, all listed in a single referral email. */
    @Valid
    private List<JobOpeningDto> jobs;

    @NotEmpty(message = "recipients must not be empty")
    @Valid
    private List<RecipientDto> recipients;
}
