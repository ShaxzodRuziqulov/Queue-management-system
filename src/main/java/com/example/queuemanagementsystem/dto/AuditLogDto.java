package com.example.queuemanagementsystem.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AuditLogDto {
    private UUID id;
    private String adminLogin;
    private String action;
    private String entityType;
    private String entityId;
    private String details;
    private Instant createdAt;
}
