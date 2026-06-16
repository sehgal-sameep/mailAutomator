package com.codewithsam.mailautomator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "email")
@Data
public class EmailProperties {

    private String subject;
    private String templatePath;
    private String resumePath;
    private String coverLetterPath;
    /** When true, logs recipients but skips actual sending */
    private boolean dryRun;
}
