package com.campus.smart_campus.dto;

import com.campus.smart_campus.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        @NotBlank(message = "Full name is required")
        @Size(min = 3, max = 120, message = "Full name must be between 3 and 120 characters")
        @Pattern(regexp = "^[A-Za-z ]+$", message = "Full name can contain only letters and spaces.")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
                message = "Password must be 8+ characters with 1 uppercase, 1 lowercase, 1 number, and 1 symbol."
        )
        String password,

        @NotNull(message = "Role is required")
        UserRole role
) {
}
