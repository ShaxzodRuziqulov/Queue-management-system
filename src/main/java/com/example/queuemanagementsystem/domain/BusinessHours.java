package com.example.queuemanagementsystem.domain;

import com.example.queuemanagementsystem.domain.enums.Weekday;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
        name = "business_hours",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_business_hours_day",
                columnNames = {"business_id", "weekday"}
        )
)
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class BusinessHours {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Weekday weekday;

    /**
     * {@code true} bo‘lsa, bu kunda ishlamaydi (ochilish/yopilish vaqtlari e’tiborsiz).
     */
    @Column(nullable = false)
    private boolean closed;

    private LocalTime opensAt;

    private LocalTime closesAt;
}
