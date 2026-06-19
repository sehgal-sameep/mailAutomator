package com.codewithsam.mailautomator.service;

import com.codewithsam.mailautomator.dto.ContactDto;
import com.codewithsam.mailautomator.dto.ReferralRequestDto;

public interface TemplateService {

    /**
     * Loads the email template and renders it with per-contact and request-level placeholders.
     */
    String render(ContactDto contact, ReferralRequestDto request);
}
