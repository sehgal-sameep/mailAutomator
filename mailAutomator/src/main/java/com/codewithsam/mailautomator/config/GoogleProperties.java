package com.codewithsam.mailautomator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google")
@Data
public class GoogleProperties {

    private Sheets sheets = new Sheets();

    @Data
    public static class Sheets {
        /** Appended to the tab name when building the A1 range, e.g. "!A:E" */
        private String rangeSuffix = "!A:E";
        /** Path to the service account JSON key file downloaded from Google Cloud Console */
        private String serviceAccountKeyPath;
    }
}
