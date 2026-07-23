package com.example.queuemanagementsystem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class Review extends BaseEntity{

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false)
    private int stars;

    @Column(columnDefinition = "text")
    private String comment;

    /**
     * Sharh tegishli xodim — bookingdan avtomatik o'rnatiladi.
     * NULL bo'lsa xodim aniqlanmagan (eski bronlar uchun).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private StaffMember staff;
}
