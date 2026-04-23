package com.example.queuemanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AppUserCreateRequest {

    @NotBlank
    @Size(min = 3, max = 64)
    private String login;

    @NotBlank
    @Size(min = 8, max = 128)
    private String password;

    @NotBlank
    @Size(max = 200)
    private String displayName;

    @Size(max = 320)
    private String email;

    @Size(max = 32)
    private String phone;

    @Size(max = 1024)
    private String avatarUrl;

    private boolean active = true;
}
