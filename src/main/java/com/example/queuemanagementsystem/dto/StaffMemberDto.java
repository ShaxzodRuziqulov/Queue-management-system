package com.example.queuemanagementsystem.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
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
    private Set<UUID> serviceIds;
    private Integer experienceYears;
    private String bio;
    private String avatarUrl;
    private Double avgRating;
    private Long reviewCount;
}
