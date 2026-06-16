package com.codewithsam.mailautomator.service;

import com.codewithsam.mailautomator.config.JobProperties;
import com.codewithsam.mailautomator.dto.ContactDto;

public interface TemplateService {

    /**
     * Loads the email template and renders it with per-contact and job-level placeholders.
     */
    String render(ContactDto contact, JobProperties jobProperties);
}
