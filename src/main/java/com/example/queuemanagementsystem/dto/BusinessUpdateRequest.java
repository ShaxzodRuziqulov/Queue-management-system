package com.example.queuemanagementsystem.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BusinessUpdateRequest {

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
