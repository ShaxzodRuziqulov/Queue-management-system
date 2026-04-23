package com.example.queuemanagementsystem.dto;

import com.example.queuemanagementsystem.domain.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class BookingCreateRequest {

    @NotNull
    private UUID customerId;

    @NotNull
    private UUID businessId;

    @NotNull
    private UUID offeredServiceId;

    private UUID staffId;

    @NotNull
    private Instant startAt;

    @NotNull
    private Instant endAt;

    private BookingStatus status;

    private String customerNote;
}
