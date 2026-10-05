package com.codewithsam.mailautomator.serviceimpl;

import com.codewithsam.mailautomator.config.EmailProperties;
import com.codewithsam.mailautomator.dto.ContactDto;
import com.codewithsam.mailautomator.dto.JobOpeningDto;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateServiceImpl implements TemplateService {

    private final EmailProperties emailProperties;

    private static final String DEFAULT_ROLE = "Software Engineer";

    @Override
    public String render(ContactDto contact, ReferralRequestDto request) {
        return render(
                contact.getFirstName(),
                contact.getLastName(),
                request.getCompanyName(),
                request.getJobs(),
                request.getTemplateType()
        );
    }

    @Override
    public String render(String firstName, String lastName, String companyName,
                         List<JobOpeningDto> jobs, TemplateType templateType) {
        String templatePath = resolveTemplatePath(templateType);
        String templateContent = loadTemplate(templatePath);

        List<JobOpeningDto> cleanJobs = jobs == null ? List.of() : jobs.stream()
                .filter(j -> j != null)
                .toList();
        String company = companyName != null ? companyName.trim() : "";

        Map<String, String> variables = new HashMap<>();
        variables.put("firstName",   firstName   != null ? firstName.trim()   : "");
        variables.put("lastName",    lastName    != null ? lastName.trim()    : "");
        variables.put("companyName", company);
        variables.put("openingsLine", buildOpeningsLine(cleanJobs, company));
        variables.put("jobDetails",   buildJobDetails(cleanJobs));
        variables.put("positionText", cleanJobs.size() > 1 ? "these positions" : "this position");

        String rendered = TemplateRenderer.render(templateContent, variables);
        // collapse 3+ consecutive newlines (left by an empty optional line) down to 2
        return rendered.replaceAll("(\r?\n)([ \t]*\r?\n){2,}", "$1\n");
    }

    private static String buildOpeningsLine(List<JobOpeningDto> jobs, String company) {
        if (jobs.size() > 1) {
            return "I came across the following openings at " + company
                    + " and found them strongly aligned with my experience:";
        }
        String role = jobs.isEmpty() || isBlank(jobs.get(0).getRoleTitle())
                ? DEFAULT_ROLE : jobs.get(0).getRoleTitle().trim();
        return "I came across the " + role + " opening at " + company
                + " and found it strongly aligned with my experience.";
    }

    /**
     * One opening renders as plain detail lines (role is already in the intro sentence);
     * several render as a numbered list, one block per opening, separated by a blank line.
     */
    private static String buildJobDetails(List<JobOpeningDto> jobs) {
        if (jobs.size() == 1) {
            return String.join("\n", detailLines(jobs.get(0), false));
        }
        List<String> blocks = new ArrayList<>();
        for (int i = 0; i < jobs.size(); i++) {
            List<String> lines = detailLines(jobs.get(i), true);
            if (lines.isEmpty()) continue;
            String indent = " ".repeat(String.valueOf(i + 1).length() + 2);
            blocks.add((i + 1) + ". " + String.join("\n" + indent, lines));
        }
        return String.join("\n\n", blocks);
    }

    private static List<String> detailLines(JobOpeningDto job, boolean includeRole) {
        List<String> lines = new ArrayList<>();
        if (includeRole) {
            lines.add("Role: " + (isBlank(job.getRoleTitle()) ? DEFAULT_ROLE : job.getRoleTitle().trim()));
        }
        if (!isBlank(job.getJobLink())) lines.add("Job Link: " + job.getJobLink().trim());
        if (!isBlank(job.getJobId()))   lines.add("Job ID: " + job.getJobId().trim());

        List<String> locations = job.getLocations() == null ? List.of() : job.getLocations().stream()
                .filter(l -> !isBlank(l))
                .map(String::trim)
                .distinct()
                .toList();
        switch (locations.size()) {
            case 0  -> { }
            case 1  -> lines.add("Location: " + locations.get(0));
            default -> lines.add("Locations: " + String.join(", ", locations));
        }
        return lines;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
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
