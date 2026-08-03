package com.example.queuemanagementsystem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Biznesning o'z mijozlar bazasi. Har bir yozuv bitta biznesga tegishli va
 * telefon raqami bo'yicha takrorlanmaydi (biznes ichida). Bron yaratilganda
 * {@code guestPhone} bo'yicha avtomatik topiladi yoki yaratiladi
 * ({@link com.example.queuemanagementsystem.service.CustomerService#upsertFromBooking}).
 */
@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customers_business_id", columnList = "business_id"),
        @Index(name = "idx_customers_business_phone", columnList = "business_id, phone")
})
@Getter
@Setter
public class Customer extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false, length = 200)
    private String fullName;

    /** Biznes ichida dedup kaliti — bo'sh bo'lishi mumkin (qo'lda kiritilgan mijoz uchun). */
    @Column(length = 32)
    private String phone;

    @Column(length = 320)
    private String email;

    /** Xodim/biznes egasi yozadigan erkin eslatma (masalan "VIP", "qisqa soch"). */
    @Column(columnDefinition = "text")
    private String note;

    /** Necha marta bron qilgan (avtomatik yig'iladi). */
    @Column(nullable = false)
    private int visitCount = 0;

    /** Oxirgi tashrif (bron) vaqti. */
    private Instant lastVisitAt;

    @Column(nullable = false)
    private boolean active = true;
}
