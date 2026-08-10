package com.example.queuemanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetConfirmRequest {
    @NotBlank
    private String login;
    @NotBlank
    private String code;
    @NotBlank
    @Size(min = 4, message = "Parol kamida 4 ta belgi bo'lishi kerak")
    private String newPassword;
}
