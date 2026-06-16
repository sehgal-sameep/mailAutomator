package com.codewithsam.mailautomator.serviceimpl;

import com.codewithsam.mailautomator.config.GoogleProperties;
import com.codewithsam.mailautomator.dto.ContactDto;
import com.codewithsam.mailautomator.exception.GoogleSheetReadException;
import com.codewithsam.mailautomator.service.ContactReaderService;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactReaderServiceImpl implements ContactReaderService {

    private static final int COL_SERIAL       = 0;
    private static final int COL_FIRST_NAME   = 1;
    private static final int COL_LAST_NAME    = 2;
    private static final int COL_COMPANY_EMAIL = 3;
    private static final int COL_PERSONAL_EMAIL = 4;
    private static final int COL_DESIGNATION  = 5;

    private final Sheets sheetsService;
    private final GoogleProperties googleProperties;

    @Override
    public List<ContactDto> readContacts() {
        String spreadsheetId = googleProperties.getSheets().getSpreadsheetId();
        String range = googleProperties.getSheets().getSheetName()
                + googleProperties.getSheets().getRangeSuffix();

        log.info("Reading contacts from Google Sheet [id={}], range: {}", spreadsheetId, range);

        ValueRange response;
        try {
            response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
        } catch (IOException ex) {
            throw new GoogleSheetReadException(
                    "Failed to read Google Sheet (id=" + spreadsheetId + "): " + ex.getMessage(), ex);
        }

        List<List<Object>> rows = response.getValues();
        if (rows == null || rows.isEmpty()) {
            log.warn("No data found in sheet range: {}", range);
            return Collections.emptyList();
        }

        log.info("Google Sheet read complete — {} rows found (including header)", rows.size());

        List<ContactDto> contacts = rows.stream()
                .skip(1)                           // skip header row
                .filter(row -> !isEmptyRow(row))
                .map(this::parseRow)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Parsed {} valid contacts from sheet", contacts.size());
        return contacts;
    }

    private boolean isEmptyRow(List<Object> row) {
        return row == null || row.stream()
                .allMatch(cell -> cell == null || cell.toString().isBlank());
    }

    private ContactDto parseRow(List<Object> row) {
        try {
            return ContactDto.builder()
                    .serialNumber(parseIntSafely(getCellValue(row, COL_SERIAL)))
                    .firstName(getCellValue(row, COL_FIRST_NAME))
                    .lastName(getCellValue(row, COL_LAST_NAME))
                    .companyEmail(getCellValue(row, COL_COMPANY_EMAIL))
                    .personalEmail(getCellValue(row, COL_PERSONAL_EMAIL))
                    .designation(getCellValue(row, COL_DESIGNATION))
                    .build();
        } catch (Exception ex) {
            log.warn("Could not parse row, skipping: {} — reason: {}", row, ex.getMessage());
            return null;
        }
    }

    private String getCellValue(List<Object> row, int index) {
        if (index >= row.size()) return null;
        Object cell = row.get(index);
        if (cell == null) return null;
        String value = cell.toString().trim();
        return value.isEmpty() ? null : value;
    }

    private int parseIntSafely(String value) {
        try {
            return value != null ? Integer.parseInt(value) : 0;
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
