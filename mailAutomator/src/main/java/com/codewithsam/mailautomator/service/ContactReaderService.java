package com.codewithsam.mailautomator.service;

import com.codewithsam.mailautomator.dto.ContactDto;

import java.util.List;

public interface ContactReaderService {

    /**
     * Reads and parses all contacts from the configured Google Sheet.
     * Skips the header row and any empty rows.
     */
    List<ContactDto> readContacts();
}
