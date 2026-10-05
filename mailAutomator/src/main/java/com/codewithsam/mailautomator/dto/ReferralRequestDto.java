package com.codewithsam.mailautomator.dto;

import jakarta.validation.Valid;
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

    /** Optional. One or more openings, all listed in a single referral email. */
    @Valid
    private List<JobOpeningDto> jobs;

    @NotBlank(message = "sheetId is required")
    private String sheetId;

    @NotBlank(message = "tabName is required")
    private String tabName;
}
