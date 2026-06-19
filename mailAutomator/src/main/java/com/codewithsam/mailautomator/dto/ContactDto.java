package com.codewithsam.mailautomator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactDto {

    private int serialNumber;
    private String firstName;
    private String lastName;
    private String companyEmail;
    private String personalEmail;
    private String designation;

    public String getFullName() {
        String first = firstName != null ? firstName.trim() : "";
        String last  = lastName  != null ? lastName.trim()  : "";
        return (first + " " + last).trim();
    }

    /**
     * Returns all non-blank email addresses for this contact.
     * Company email comes first. Personal email is included only if it
     * differs from the company email (case-insensitive) to avoid sending
     * the same address twice from a single row.
     */
    public List<String> getValidEmails() {
        List<String> emails = new ArrayList<>();
        if (companyEmail != null && !companyEmail.isBlank()) {
            emails.add(companyEmail.trim());
        }
        if (personalEmail != null && !personalEmail.isBlank()
                && !personalEmail.trim().equalsIgnoreCase(companyEmail == null ? "" : companyEmail.trim())) {
            emails.add(personalEmail.trim());
        }
        return emails;
    }
}
