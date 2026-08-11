package com.example.queuemanagementsystem.dto;

import com.example.queuemanagementsystem.domain.enums.BusinessCategory;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class PublicBusinessSummaryDto {
    private UUID id;
    private String name;
    private String description;
    private String addressLine;
    private String city;
    private String contactPhone;
    private BusinessCategory category;
    private String imageUrl;
    private long serviceCount;
    private double avgRating;
    private long reviewCount;
}
