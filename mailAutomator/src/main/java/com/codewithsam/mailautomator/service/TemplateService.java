package com.codewithsam.mailautomator.service;

import com.codewithsam.mailautomator.dto.ContactDto;
import com.codewithsam.mailautomator.dto.ReferralRequestDto;
import com.codewithsam.mailautomator.dto.TemplateType;

public interface TemplateService {

    /**
     * Convenience overload for the sheet-based flow.
     * Delegates to {@link #render(String, String, String, String, String, TemplateType)}.
     */
    String render(ContactDto contact, ReferralRequestDto request);

    /**
     * Core render method — loads the template for {@code templateType} and substitutes
     * all {@code {{placeholder}}} values with the supplied arguments.
     */
    String render(String firstName, String lastName, String companyName,
                  String jobId, String jobLink, TemplateType templateType);
}
