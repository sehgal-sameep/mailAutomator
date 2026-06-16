package com.codewithsam.mailautomator.serviceimpl;

import com.codewithsam.mailautomator.config.EmailProperties;
import com.codewithsam.mailautomator.config.JobProperties;
import com.codewithsam.mailautomator.dto.ContactDto;
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
    public String render(ContactDto contact, JobProperties jobProperties) {
        String templateContent = loadTemplate(emailProperties.getTemplatePath());

        Map<String, String> variables = new HashMap<>();
        variables.put("firstName", contact.getFirstName());
        variables.put("lastName", contact.getLastName());
        variables.put("companyName", jobProperties.getCompanyName());
        variables.put("jobId", jobProperties.getJobId());
        variables.put("jobLink", jobProperties.getJobLink());

        return TemplateRenderer.render(templateContent, variables);
    }

    private String loadTemplate(String templatePath) {
        // Try filesystem first (absolute or working-directory-relative path)
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

        // Fall back to classpath (e.g. src/main/resources/templates/...)
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
