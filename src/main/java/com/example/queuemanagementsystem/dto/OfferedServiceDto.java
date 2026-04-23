package com.example.queuemanagementsystem.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class OfferedServiceDto {
    private UUID id;
    private UUID businessId;
    private String name;
    private String description;
    private int durationMinutes;
    private BigDecimal basePrice;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
