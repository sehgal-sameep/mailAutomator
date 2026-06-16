package com.codewithsam.mailautomator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gmail")
@Data
public class GmailProperties {

    /** Gmail address used as the From: header on every outgoing email. */
    private String senderEmail;
}
