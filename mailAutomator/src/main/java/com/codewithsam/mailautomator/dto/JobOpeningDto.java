package com.codewithsam.mailautomator.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

import java.util.List;

/** A single job opening referenced in a referral email. All fields are optional. */
@Data
public class JobOpeningDto {

    /** Optional. Role title, e.g. "Software Engineer II". */
    private String roleTitle;

    private String jobId;

    /** Optional. Must be a valid URL when provided. */
    private String jobLink;

    /** Optional. One or more locations, rendered as a "Location(s): ..." line. */
    private List<String> locations;

    @AssertTrue(message = "jobLink must be a valid URL starting with http:// or https://")
    private boolean isJobLinkValid() {
        return jobLink == null || jobLink.isBlank() || jobLink.matches("^https?://.+");
    }
}
