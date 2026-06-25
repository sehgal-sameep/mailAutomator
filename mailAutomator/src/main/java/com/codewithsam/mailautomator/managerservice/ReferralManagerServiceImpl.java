package com.codewithsam.mailautomator.managerservice;

import com.codewithsam.mailautomator.config.EmailProperties;
import com.codewithsam.mailautomator.dto.ContactDto;
import com.codewithsam.mailautomator.dto.ManualReferralRequestDto;
import com.codewithsam.mailautomator.dto.ManualReferralSummaryDto;
import com.codewithsam.mailautomator.dto.RecipientDto;
import com.codewithsam.mailautomator.dto.ReferralRequestDto;
import com.codewithsam.mailautomator.dto.ReferralSummaryDto;
import com.codewithsam.mailautomator.dto.TemplateType;
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
    private final EmailProperties emailProperties;

    // -------------------------------------------------------------------------
    // Sheet-based flow
    // -------------------------------------------------------------------------

    @Override
    public ReferralSummaryDto sendReferralEmails(ReferralRequestDto request) {
        log.info("Starting referral email process — company: {}, jobId: {}, sheet: {}/{}",
                request.getCompanyName(), request.getJobId(), request.getSheetId(), request.getTabName());

        List<ContactDto> contacts = contactReaderService.readContacts(request.getSheetId(), request.getTabName());
        log.info("Total contacts loaded: {}", contacts.size());

        if (emailProperties.isDryRun()) {
            log.info("[DRY RUN MODE] No emails will be sent");
        }

        RunContext ctx = new RunContext(buildSubject(request), resolveAttachments());

        for (ContactDto contact : contacts) {
            processContact(contact, request, ctx);
        }

        log.info("Process complete — records: {}, recipients: {}, sent: {}, rowsSkipped: {}, duplicates: {}, failed: {}",
                contacts.size(), ctx.totalEmailAddresses, ctx.emailsSent,
                ctx.itemsSkipped, ctx.duplicatesSkipped, ctx.failedEmails.size());

        return ReferralSummaryDto.builder()
                .totalRecords(contacts.size())
                .totalRecipients(ctx.totalEmailAddresses)
                .emailsSent(ctx.emailsSent)
                .rowsSkipped(ctx.itemsSkipped)
                .duplicatesSkipped(ctx.duplicatesSkipped)
                .failedEmails(ctx.failedEmails)
                .build();
    }

    private void processContact(ContactDto contact, ReferralRequestDto request, RunContext ctx) {
        List<String> validAddresses = contact.getValidEmails().stream()
                .filter(EmailValidator::isValid)
                .toList();

        if (validAddresses.isEmpty()) {
            log.warn("Skipping '{}' — no valid email address found", contact.getFullName());
            ctx.itemsSkipped++;
            return;
        }

        log.info("'{}' — {} valid address(es): {}", contact.getFullName(), validAddresses.size(), validAddresses);
        ctx.totalEmailAddresses += validAddresses.size();

        String body = emailProperties.isDryRun() ? null : templateService.render(contact, request);

        for (String email : validAddresses) {
            dispatchEmail(email, contact.getFullName(), body, ctx);
        }
    }

    private String buildSubject(ReferralRequestDto request) {
        return switch (request.getTemplateType()) {
            case REFERRAL         -> "Referral Request - " + request.getCompanyName() + " | Job ID: " + request.getJobId();
            case INTERNAL_OPENING -> "Internal Openings Enquiry - " + request.getCompanyName();
        };
    }

    // -------------------------------------------------------------------------
    // Manual flow
    // -------------------------------------------------------------------------

    @Override
    public ManualReferralSummaryDto sendReferralEmailsManual(ManualReferralRequestDto request) {
        log.info("Starting manual referral email process — company: {}, jobId: {}, recipients: {}",
                request.getCompanyName(), request.getJobId(), request.getRecipients().size());

        if (emailProperties.isDryRun()) {
            log.info("[DRY RUN MODE] No emails will be sent");
        }

        String subject = "Referral Request - " + request.getCompanyName() + " | Job ID: " + request.getJobId();
        RunContext ctx = new RunContext(subject, resolveAttachments());

        for (RecipientDto recipient : request.getRecipients()) {
            processRecipient(recipient, request, ctx);
        }

        log.info("Manual process complete — recipients: {}, emailAddresses: {}, sent: {}, skipped: {}, duplicates: {}, failed: {}",
                request.getRecipients().size(), ctx.totalEmailAddresses, ctx.emailsSent,
                ctx.itemsSkipped, ctx.duplicatesSkipped, ctx.failedEmails.size());

        return ManualReferralSummaryDto.builder()
                .totalRecipients(request.getRecipients().size())
                .totalEmailAddresses(ctx.totalEmailAddresses)
                .emailsSent(ctx.emailsSent)
                .emailsSkipped(ctx.itemsSkipped)
                .duplicateEmailsSkipped(ctx.duplicatesSkipped)
                .failedEmails(ctx.failedEmails)
                .build();
    }

    private void processRecipient(RecipientDto recipient, ManualReferralRequestDto request, RunContext ctx) {
        String fullName = buildFullName(recipient.getFirstName(), recipient.getLastName());

        List<String> validAddresses = recipient.getEmails().stream()
                .filter(e -> e != null && !e.isBlank())
                .map(String::trim)
                .distinct()
                .filter(EmailValidator::isValid)
                .toList();

        if (validAddresses.isEmpty()) {
            log.warn("Skipping '{}' — no valid email address found", fullName);
            ctx.itemsSkipped++;
            return;
        }

        log.info("'{}' — {} valid address(es): {}", fullName, validAddresses.size(), validAddresses);
        ctx.totalEmailAddresses += validAddresses.size();

        String body = emailProperties.isDryRun() ? null
                : templateService.render(recipient.getFirstName(), recipient.getLastName(),
                                         request.getCompanyName(), request.getJobId(),
                                         request.getJobLink(), TemplateType.REFERRAL);

        for (String email : validAddresses) {
            dispatchEmail(email, fullName, body, ctx);
        }
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    private void dispatchEmail(String email, String contactName, String body, RunContext ctx) {
        String key = email.toLowerCase();

        if (ctx.sentInThisRun.contains(key)) {
            log.warn("Skipping duplicate email in this run: {}", email);
            ctx.duplicatesSkipped++;
        } else if (emailProperties.isDryRun()) {
            log.info("[DRY RUN] Would send to: {} <{}>", contactName, email);
            ctx.emailsSent++;
            ctx.sentInThisRun.add(key);
        } else {
            sendAndRecord(email, contactName, ctx.subject, body, ctx.attachments, ctx);
        }
    }

    private void sendAndRecord(String email, String contactName, String subject,
                               String body, List<File> attachments, RunContext ctx) {
        try {
            emailService.sendEmail(email, subject, body, attachments);
            ctx.sentInThisRun.add(email.toLowerCase());
            ctx.emailsSent++;
            log.info("Email sent successfully to: {} <{}>", contactName, email);
        } catch (Exception ex) {
            log.error("Failed to send email to {} <{}>: {}", contactName, email, ex.getMessage());
            ctx.failedEmails.add(email);
        }
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

    private static String buildFullName(String firstName, String lastName) {
        String first = firstName != null ? firstName.trim() : "";
        String last  = lastName  != null ? lastName.trim()  : "";
        return (first + " " + last).trim();
    }

    /** Mutable run-scoped state threaded through the processing methods to avoid a long parameter list. */
    private static class RunContext {
        final String subject;
        final List<File> attachments;
        final Set<String> sentInThisRun = new HashSet<>();
        final List<String> failedEmails  = new ArrayList<>();

        int totalEmailAddresses = 0;
        int emailsSent          = 0;
        int itemsSkipped        = 0;
        int duplicatesSkipped   = 0;

        RunContext(String subject, List<File> attachments) {
            this.subject     = subject;
            this.attachments = attachments;
        }
    }
}
