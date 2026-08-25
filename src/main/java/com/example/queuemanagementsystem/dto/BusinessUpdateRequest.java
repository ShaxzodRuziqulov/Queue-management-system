package com.example.queuemanagementsystem.dto;

import com.example.queuemanagementsystem.domain.enums.BusinessCategory;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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

    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private BigDecimal latitude;

    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private BigDecimal longitude;

    @Size(max = 32)
    private String contactPhone;

    private BusinessCategory category;
}
