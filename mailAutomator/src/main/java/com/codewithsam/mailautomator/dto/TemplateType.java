package com.codewithsam.mailautomator.dto;

public enum TemplateType {

    /** Sends a referral request for a specific job opening — requires jobId and jobLink. */
    REFERRAL,

    /** Asks for a referral for any internal openings — jobId and jobLink are not required. */
    INTERNAL_OPENING
}
