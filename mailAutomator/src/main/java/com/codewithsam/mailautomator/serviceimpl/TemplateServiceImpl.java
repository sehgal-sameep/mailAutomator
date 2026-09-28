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
import java.util.List;
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
                request.getLocations(),
                request.getTemplateType()
        );
    }

    @Override
    public String render(String firstName, String lastName, String companyName,
                         String jobId, String jobLink, List<String> locations,
                         TemplateType templateType) {
        String templatePath = resolveTemplatePath(templateType);
        String templateContent = loadTemplate(templatePath);

        boolean hasJobId = jobId != null && !jobId.isBlank();
        boolean hasJobLink = jobLink != null && !jobLink.isBlank();
        List<String> cleanLocations = locations == null ? List.of() : locations.stream()
                .filter(l -> l != null && !l.isBlank())
                .map(String::trim)
                .distinct()
                .toList();

        Map<String, String> variables = new HashMap<>();
        variables.put("firstName",   firstName   != null ? firstName.trim()   : "");
        variables.put("lastName",    lastName    != null ? lastName.trim()    : "");
        variables.put("companyName", companyName != null ? companyName.trim() : "");
        variables.put("jobLinkLine", hasJobLink  ? "Job Link: " + jobLink.trim() : "");
        variables.put("jobIdLine",   hasJobId    ? "Job ID: " + jobId.trim() : "");
        variables.put("locationLine", switch (cleanLocations.size()) {
            case 0  -> "";
            case 1  -> "Location: " + cleanLocations.get(0);
            default -> "Locations: " + String.join(", ", cleanLocations);
        });

        String rendered = TemplateRenderer.render(templateContent, variables);
        // collapse 3+ consecutive newlines (left by an empty optional line) down to 2
        return rendered.replaceAll("(\r?\n)([ \t]*\r?\n){2,}", "$1\n");
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
