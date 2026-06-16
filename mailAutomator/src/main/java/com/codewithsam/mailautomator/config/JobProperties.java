package com.codewithsam.mailautomator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "job")
@Data
public class JobProperties {

    private String companyName;
    private String jobId;
    private String jobLink;
}
