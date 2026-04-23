package com.example.queuemanagementsystem.dto;

import com.example.queuemanagementsystem.domain.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class BookingDto {
    private UUID id;
    private UUID customerId;
    private UUID businessId;
    private UUID offeredServiceId;
    private UUID staffId;
    private Instant startAt;
    private Instant endAt;
    private BookingStatus status;
    private String customerNote;
    private Instant createdAt;
    private Instant updatedAt;
}
