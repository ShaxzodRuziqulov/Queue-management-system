package com.example.queuemanagementsystem.dto;

import com.example.queuemanagementsystem.domain.enums.BusinessStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class BusinessStatusRequest {

    @NotNull
    private BusinessStatus status;

    /** ACTIVE holatida obuna tugash sanasi (ixtiyoriy — null bo'lsa cheksiz). */
    private Instant subscriptionEndDate;
}