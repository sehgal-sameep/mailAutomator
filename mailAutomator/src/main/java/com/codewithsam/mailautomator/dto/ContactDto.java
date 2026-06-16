package com.codewithsam.mailautomator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    /** Company email is preferred; falls back to personal email. */
    public String getPrimaryEmail() {
        if (companyEmail != null && !companyEmail.isBlank()) {
            return companyEmail;
        }
        return personalEmail;
    }

    public String getFullName() {
        String first = firstName != null ? firstName.trim() : "";
        String last = lastName != null ? lastName.trim() : "";
        return (first + " " + last).trim();
    }
}
