package com.example.queuemanagementsystem.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ReviewDto {
    private UUID id;
    private UUID bookingId;
    private UUID businessId;
    private int stars;
    private String comment;
    private UUID staffId;
    private String staffName;
    private Instant createdAt;
}
