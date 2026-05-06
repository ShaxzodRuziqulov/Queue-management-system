package com.example.queuemanagementsystem.dto;

import com.example.queuemanagementsystem.domain.enums.BusinessStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class BusinessDto {
    private UUID id;
    private UUID ownerId;
    private String name;
    private String description;
    private String addressLine;
    private String city;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String contactPhone;
    private BusinessStatus status;
    private Instant trialEndDate;
    private Instant subscriptionEndDate;
    private boolean accessAllowed;
    private Instant createdAt;
    private Instant updatedAt;
}
