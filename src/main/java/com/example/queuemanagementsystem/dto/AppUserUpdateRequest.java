package com.example.queuemanagementsystem.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class AppUserUpdateRequest {

    @Size(min = 8, max = 128)
    private String password;

    @Size(max = 120)
    private String firstName;

    @Size(max = 120)
    private String lastName;

    @Size(max = 200)
    private String displayName;

    @Size(max = 320)
    private String email;

    @Size(max = 32)
    private String phone;

    @Size(max = 1024)
    private String avatarUrl;

    private Boolean active;

    /** Admin tomonidan rol o'zgartirish uchun (e.g. "ROLE_ADMIN", "ROLE_BUSINESS_OWNER") */
    private Set<String> roles;
}
