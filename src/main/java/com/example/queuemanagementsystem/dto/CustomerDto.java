package com.example.queuemanagementsystem.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class CustomerDto {
    private UUID id;
    private UUID businessId;
    private String fullName;
    private String phone;
    private String email;
    private String note;
    private int visitCount;
    private Instant lastVisitAt;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
