package com.example.queuemanagementsystem.dto;

import com.example.queuemanagementsystem.domain.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class BookingCreateRequest {

    /** Ro'yxatdan o'tgan mijoz uchun (hozircha ishlatilmaydi — kelajakdagi mijoz ilovasi uchun). */
    private UUID customerId;

    /** Hisobsiz (mehmon) mijoz ismi — customerId berilmagan bo'lsa majburiy. */
    @Size(max = 200)
    private String guestName;

    @Size(max = 32)
    private String guestPhone;

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
