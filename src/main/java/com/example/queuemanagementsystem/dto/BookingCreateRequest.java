package com.example.queuemanagementsystem.dto;

import com.example.queuemanagementsystem.domain.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class BookingCreateRequest {

    /** Biznes mijozlar bazasidagi mavjud mijoz. */
    private UUID customerId;

    /** Yangi mijoz yaratish/topish uchun ma'lumotlar (customerId berilmasa). */
    @Size(max = 120)
    private String customerFirstName;

    @Size(max = 120)
    private String customerLastName;

    @Size(max = 120)
    private String customerMiddleName;

    @Size(max = 32)
    private String customerPhone;

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
