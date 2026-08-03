package com.example.queuemanagementsystem.domain;

import com.example.queuemanagementsystem.domain.enums.BookingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "bookings",
        indexes = {
                @Index(name = "idx_bookings_business_start", columnList = "business_id, start_at"),
                @Index(name = "idx_bookings_customer_start", columnList = "customer_id, start_at")
        }
)
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class Booking extends BaseEntity{

    /**
     * Ro'yxatdan o'tgan mijoz hisobi — hozircha ishlatilmaydi (mijoz ilovasi alohida loyiha
     * sifatida rejalashtirilgan, keyinchalik ulanadi). Bron odatda {@link #guestName} orqali
     * xodim/biznes egasi tomonidan kiritiladi.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private AppUser customer;

    /** Hisobsiz (mehmon) mijozning ismi — customer bog'lanmagan bronlar uchun majburiy. */
    @Column(name = "guest_name", length = 200)
    private String guestName;

    /** Hisobsiz mijozning telefon raqami (ixtiyoriy). */
    @Column(name = "guest_phone", length = 32)
    private String guestPhone;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "offered_service_id", nullable = false)
    private OfferedService offeredService;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private StaffMember staff;

    /**
     * Biznesning mijozlar bazasidagi yozuv. Bron yaratilganda {@link #guestPhone}
     * bo'yicha avtomatik topiladi yoki yaratiladi (telefon bo'lmasa — null).
     * Yuqoridagi {@link #customer} dan farqli — bu biznesga tegishli mijoz profili,
     * {@link #customer} esa kelajakdagi mijoz ilovasining tizim hisobi.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_customer_id")
    private Customer client;

    @Column(nullable = false)
    private Instant startAt;

    @Column(nullable = false)
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(columnDefinition = "text")
    private String customerNote;
}
