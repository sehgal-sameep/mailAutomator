package com.codewithsam.mailautomator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class RecipientDto {

    @NotBlank(message = "recipient firstName must not be blank")
    private String firstName;

    private String lastName;

    @NotEmpty(message = "recipient must have at least one email address")
    private List<String> emails;
}
