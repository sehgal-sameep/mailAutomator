package com.codewithsam.mailautomator.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class GoogleApiConfig {

    private static final String APPLICATION_NAME = "Mail Automator";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final GoogleProperties googleProperties;

    @Bean
    public Sheets googleSheets() throws IOException, GeneralSecurityException {
        String keyPath = googleProperties.getSheets().getServiceAccountKeyPath();
        File keyFile = new File(keyPath);

        if (!keyFile.exists()) {
            throw new IllegalStateException(
                    "Google service account key not found at: " + keyPath +
                    "\nTo fix: Google Cloud Console → IAM & Admin → Service Accounts" +
                    " → Keys → Add Key → JSON. Save as google-service-account.json.");
        }

        log.info("Loading Google Sheets credentials from service account key: {}", keyFile.getAbsolutePath());

        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(keyFile))
                .createScoped(Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY));

        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();

        return new Sheets.Builder(transport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
