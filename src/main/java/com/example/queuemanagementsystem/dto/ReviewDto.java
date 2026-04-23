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
    private int stars;
    private String comment;
    private Instant createdAt;
}
