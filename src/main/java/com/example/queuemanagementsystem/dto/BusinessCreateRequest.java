package com.example.queuemanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BusinessCreateRequest {

    @NotNull
    private UUID ownerId;

    @NotBlank
    @Size(max = 200)
    private String name;

    private String description;

    @Size(max = 500)
    private String addressLine;

    @Size(max = 120)
    private String city;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @Size(max = 32)
    private String contactPhone;
}
