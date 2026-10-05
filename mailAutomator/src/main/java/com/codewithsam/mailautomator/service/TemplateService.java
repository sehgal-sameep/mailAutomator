package com.codewithsam.mailautomator.service;

import com.codewithsam.mailautomator.dto.ContactDto;
import com.codewithsam.mailautomator.dto.JobOpeningDto;
import com.codewithsam.mailautomator.dto.ReferralRequestDto;
import com.codewithsam.mailautomator.dto.TemplateType;

import java.util.List;

public interface TemplateService {

    /**
     * Convenience overload for the sheet-based flow.
     * Delegates to {@link #render(String, String, String, List, TemplateType)}.
     */
    String render(ContactDto contact, ReferralRequestDto request);

    /**
     * Core render method — loads the template for {@code templateType} and substitutes
     * all {@code {{placeholder}}} values with the supplied arguments. {@code jobs} may be null or empty.
     */
    String render(String firstName, String lastName, String companyName,
                  List<JobOpeningDto> jobs, TemplateType templateType);
}
