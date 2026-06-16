package com.codewithsam.mailautomator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MailAutomatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailAutomatorApplication.class, args);
    }
}
