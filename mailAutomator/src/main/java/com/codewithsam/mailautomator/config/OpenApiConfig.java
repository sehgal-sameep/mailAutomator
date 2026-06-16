package com.codewithsam.mailautomator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mailAutomatorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mail Automator API")
                        .description("Automated referral email sender using Google Sheets and Gmail SMTP")
                        .version("0.0.1"));
    }
}
