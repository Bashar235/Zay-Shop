package com.example.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class UserProfileDTO {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @jakarta.validation.constraints.Email(message = "Invalid email format")
    private String email;
}