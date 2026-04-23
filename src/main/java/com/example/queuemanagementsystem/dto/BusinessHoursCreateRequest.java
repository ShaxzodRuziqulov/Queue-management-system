package com.example.queuemanagementsystem.dto;

import com.example.queuemanagementsystem.domain.enums.Weekday;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class BusinessHoursCreateRequest {

    @NotNull
    private Weekday weekday;

    private boolean closed;

    private LocalTime opensAt;
    private LocalTime closesAt;
}
