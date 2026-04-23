package com.example.queuemanagementsystem.dto;

import com.example.queuemanagementsystem.domain.enums.BookingStatus;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class BookingUpdateRequest {
    private UUID staffId;
    private Instant startAt;
    private Instant endAt;
    private BookingStatus status;
    private String customerNote;
}
