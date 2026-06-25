package com.codewithsam.mailautomator.serviceimpl;

import com.codewithsam.mailautomator.config.EmailProperties;
import com.codewithsam.mailautomator.dto.ContactDto;
import com.codewithsam.mailautomator.dto.ReferralRequestDto;
import com.codewithsam.mailautomator.dto.TemplateType;
import com.codewithsam.mailautomator.exception.TemplateRenderException;
import com.codewithsam.mailautomator.service.TemplateService;
import com.codewithsam.mailautomator.util.TemplateRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateServiceImpl implements TemplateService {

    private final EmailProperties emailProperties;

    @Override
    public String render(ContactDto contact, ReferralRequestDto request) {
        return render(
                contact.getFirstName(),
                contact.getLastName(),
                request.getCompanyName(),
                request.getJobId(),
                request.getJobLink(),
                request.getTemplateType()
        );
    }

    @Override
    public String render(String firstName, String lastName, String companyName,
                         String jobId, String jobLink, TemplateType templateType) {
        String templatePath = resolveTemplatePath(templateType);
        String templateContent = loadTemplate(templatePath);

        Map<String, String> variables = new HashMap<>();
        variables.put("firstName",   firstName  != null ? firstName  : "");
        variables.put("lastName",    lastName   != null ? lastName   : "");
        variables.put("companyName", companyName != null ? companyName : "");
        variables.put("jobId",       jobId      != null ? jobId      : "");
        variables.put("jobLink",     jobLink    != null ? jobLink    : "");

        return TemplateRenderer.render(templateContent, variables);
    }

    private String resolveTemplatePath(TemplateType type) {
        return switch (type) {
            case REFERRAL         -> emailProperties.getReferralTemplatePath();
            case INTERNAL_OPENING -> emailProperties.getInternalOpeningTemplatePath();
        };
    }

    private String loadTemplate(String templatePath) {
        File file = new File(templatePath);
        if (file.exists()) {
            try {
                log.debug("Loading template from filesystem: {}", file.getAbsolutePath());
                return Files.readString(file.toPath());
            } catch (IOException ex) {
                throw new TemplateRenderException(
                        "Failed to read template file: " + file.getAbsolutePath(), ex);
            }
        }

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(templatePath)) {
            if (is == null) {
                throw new TemplateRenderException(
                        "Template not found on filesystem or classpath: " + templatePath);
            }
            log.debug("Loading template from classpath: {}", templatePath);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new TemplateRenderException("Failed to read template from classpath: " + templatePath, ex);
        }
    }
}
