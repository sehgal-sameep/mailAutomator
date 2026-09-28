package com.codewithsam.mailautomator.service;

import com.codewithsam.mailautomator.dto.ContactDto;
import com.codewithsam.mailautomator.dto.ReferralRequestDto;
import com.codewithsam.mailautomator.dto.TemplateType;

import java.util.List;

public interface TemplateService {

    /**
     * Convenience overload for the sheet-based flow.
     * Delegates to {@link #render(String, String, String, String, String, List, TemplateType)}.
     */
    String render(ContactDto contact, ReferralRequestDto request);

    /**
     * Core render method — loads the template for {@code templateType} and substitutes
     * all {@code {{placeholder}}} values with the supplied arguments.
     */
    String render(String firstName, String lastName, String companyName,
                  String jobId, String jobLink, List<String> locations, TemplateType templateType);
}
