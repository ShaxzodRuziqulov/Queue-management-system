package com.example.queuemanagementsystem.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class AppUserDto {
    private UUID id;
    private String login;
    private String firstName;
    private String lastName;
    private String displayName;
    private String email;
    private String phone;
    private String avatarUrl;
    private boolean active;
    private boolean businessOwner;
    private Set<String> roles;
    private Instant createdAt;
    private Instant updatedAt;
}
