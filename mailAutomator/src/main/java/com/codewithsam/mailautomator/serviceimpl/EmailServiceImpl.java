package com.codewithsam.mailautomator.serviceimpl;

import com.codewithsam.mailautomator.config.GmailProperties;
import com.codewithsam.mailautomator.exception.EmailSendException;
import com.codewithsam.mailautomator.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final GmailProperties gmailProperties;

    @Override
    public void sendEmail(String to, String subject, String body, List<File> attachments) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(gmailProperties.getSenderEmail());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);

            for (File attachment : attachments) {
                if (!attachment.exists()) {
                    log.warn("Attachment not found, skipping: {}", attachment.getAbsolutePath());
                    continue;
                }
                helper.addAttachment(attachment.getName(), attachment);
                log.debug("Attached: {}", attachment.getName());
            }

            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (MessagingException ex) {
            throw new EmailSendException("Failed to send email to " + to + ": " + ex.getMessage(), ex);
        }
    }
}
