package com.example.queuemanagementsystem.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class BusinessHoursUpdateRequest {
    private Boolean closed;
    private LocalTime opensAt;
    private LocalTime closesAt;
}
