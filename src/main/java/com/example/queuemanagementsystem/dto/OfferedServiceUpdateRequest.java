package com.example.queuemanagementsystem.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OfferedServiceUpdateRequest {

    @Size(max = 200)
    private String name;

    private String description;

    @Positive
    private Integer durationMinutes;

    private BigDecimal basePrice;

    private Boolean active;

    @Size(max = 1024)
    private String imageUrl;
}
