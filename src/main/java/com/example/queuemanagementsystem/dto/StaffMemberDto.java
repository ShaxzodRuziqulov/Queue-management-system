package com.example.queuemanagementsystem.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class StaffMemberDto {
    private UUID id;
    private UUID businessId;
    private String displayName;
    private UUID linkedUserId;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
