package com.example.queuemanagementsystem.dto;

import com.example.queuemanagementsystem.domain.enums.AuthProvider;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class UserIdentityDto {
    private UUID id;
    private UUID userId;
    private AuthProvider provider;
    private String providerSubject;
    private String providerEmail;
    private Instant linkedAt;
}
