package com.example.queuemanagementsystem.dto;

import com.example.queuemanagementsystem.domain.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class BookingAvailabilityDto {
    private UUID id;
    private UUID staffId;
    private Instant startAt;
    private Instant endAt;
    private BookingStatus status;
}
