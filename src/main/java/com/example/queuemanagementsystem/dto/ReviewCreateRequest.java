package com.example.queuemanagementsystem.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReviewCreateRequest {

    @NotNull
    private UUID bookingId;

    @Min(1)
    @Max(5)
    private int stars;

    private String comment;
}
