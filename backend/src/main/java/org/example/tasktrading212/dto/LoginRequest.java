package org.example.tasktrading212.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Email cannot be blank")
        String username,

        @NotBlank(message = "Password cannot be blank")
        @Size(min = 5, max = 10, message = "Password should be between 5 and 10 characters")
        String password
) {
}
