package com.codewithsam.mailautomator.service;

import java.io.File;
import java.util.List;

public interface EmailService {

    /**
     * Sends an email via Gmail API with optional file attachments.
     *
     * @param to          recipient email address
     * @param subject     email subject line
     * @param body        plain-text email body
     * @param attachments list of files to attach (non-existent files are skipped with a warning)
     */
    void sendEmail(String to, String subject, String body, List<File> attachments);
}
