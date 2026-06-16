package com.codewithsam.mailautomator.managerservice;

import com.codewithsam.mailautomator.config.EmailProperties;
import com.codewithsam.mailautomator.config.JobProperties;
import com.codewithsam.mailautomator.dto.ContactDto;
import com.codewithsam.mailautomator.dto.ReferralSummaryDto;
import com.codewithsam.mailautomator.service.ContactReaderService;
import com.codewithsam.mailautomator.service.EmailService;
import com.codewithsam.mailautomator.service.TemplateService;
import com.codewithsam.mailautomator.util.EmailValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReferralManagerServiceImpl implements ReferralManagerService {

    private final ContactReaderService contactReaderService;
    private final EmailService emailService;
    private final TemplateService templateService;
    private final JobProperties jobProperties;
    private final EmailProperties emailProperties;

    @Override
    public ReferralSummaryDto sendReferralEmails() {
        log.info("Starting referral email process — company: {}", jobProperties.getCompanyName());

        List<ContactDto> contacts = contactReaderService.readContacts();
        log.info("Total contacts loaded: {}", contacts.size());

        if (emailProperties.isDryRun()) {
            log.info("[DRY RUN MODE] No emails will be sent");
        }

        List<File> attachments = resolveAttachments();

        int totalRecords  = contacts.size();
        int emailsSent    = 0;
        int emailsSkipped = 0;
        List<String> failedEmails = new ArrayList<>();
        Set<String> sentInThisRun = new HashSet<>();

        for (ContactDto contact : contacts) {
            String primaryEmail = contact.getPrimaryEmail();

            if (EmailValidator.isInvalid(primaryEmail)) {
                log.warn("Skipping '{}' — no valid email address found", contact.getFullName());
                emailsSkipped++;
                continue;
            }

            if (sentInThisRun.contains(primaryEmail)) {
                log.warn("Skipping duplicate email in this run: {}", primaryEmail);
                emailsSkipped++;
                continue;
            }

            if (emailProperties.isDryRun()) {
                log.info("[DRY RUN] Would send to: {} <{}>", contact.getFullName(), primaryEmail);
                emailsSent++;
                sentInThisRun.add(primaryEmail);
                continue;
            }

            try {
                String body = templateService.render(contact, jobProperties);
                emailService.sendEmail(primaryEmail, emailProperties.getSubject(), body, attachments);
                sentInThisRun.add(primaryEmail);
                emailsSent++;
            } catch (Exception ex) {
                log.error("Failed to send email to {}: {}", primaryEmail, ex.getMessage());
                failedEmails.add(primaryEmail);
            }
        }

        log.info("Referral email process complete — sent: {}, skipped: {}, failed: {}",
                emailsSent, emailsSkipped, failedEmails.size());

        return ReferralSummaryDto.builder()
                .totalRecords(totalRecords)
                .emailsSent(emailsSent)
                .emailsSkipped(emailsSkipped)
                .failedEmails(failedEmails)
                .build();
    }

    private List<File> resolveAttachments() {
        List<File> attachments = new ArrayList<>();
        resolveFile(emailProperties.getResumePath(), "resume", attachments);
        resolveFile(emailProperties.getCoverLetterPath(), "cover letter", attachments);
        return attachments;
    }

    private void resolveFile(String path, String label, List<File> result) {
        if (path == null || path.isBlank()) return;

        File file = new File(path);
        if (file.exists()) {
            result.add(file);
            log.info("Loaded {} from: {}", label, file.getAbsolutePath());
            return;
        }

        // Fall back to classpath resource
        URL resource = getClass().getClassLoader().getResource(path);
        if (resource != null) {
            try {
                result.add(new File(resource.toURI()));
                log.info("Loaded {} from classpath: {}", label, path);
            } catch (Exception ex) {
                log.warn("Could not resolve classpath URI for {}: {}. Attachment will be skipped.", label, path);
            }
        } else {
            log.warn("Attachment not found for {}: {}. It will not be included.", label, path);
        }
    }
}
