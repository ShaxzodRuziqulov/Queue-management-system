package com.example.queuemanagementsystem.dto;

import com.example.queuemanagementsystem.domain.enums.Weekday;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class BusinessHoursDto {
    private UUID id;
    private UUID businessId;
    private Weekday weekday;
    private boolean closed;
    private LocalTime opensAt;
    private LocalTime closesAt;
}
