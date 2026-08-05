package com.example.queuemanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class StaffRegisterRequest {

    @NotBlank
    @Size(max = 120)
    private String firstName;

    @Size(max = 120)
    private String lastName;

    @NotBlank
    @Size(min = 3, max = 64)
    private String login;

    @NotBlank
    @Size(min = 4, max = 128)
    private String password;

    @Size(max = 320)
    private String email;

    @Size(max = 32)
    private String phone;

    @Size(max = 1024)
    private String avatarUrl;

    private String bio;

    private Integer experienceYears;

    private Set<UUID> serviceIds = new HashSet<>();
}
