package com.example.queuemanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OfferedServiceCreateRequest {

    @NotBlank
    @Size(max = 200)
    private String name;

    private String description;

    @Positive
    private int durationMinutes;

    private BigDecimal basePrice;

    private boolean active = true;
}
