package com.example.queuemanagementsystem.dto.auth;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder
public class LoginResponse {
    String accessToken;
    String tokenType;
    long expiresInSeconds;
    UUID userId;
    String login;
    boolean businessOwner;
    boolean admin;
    List<String> roles;
}
