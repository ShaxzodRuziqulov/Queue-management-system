package com.example.queuemanagementsystem.domain;

import jakarta.persistence.*;
import com.example.queuemanagementsystem.domain.enums.SupportSource;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Frontend yaratadigan, Telegram deep-link uchun qisqa umrli bir martalik kalit. */
@Entity
@Table(name = "telegram_link_token")
@Getter
@Setter
public class TelegramLinkToken extends BaseEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SupportSource source = SupportSource.GENERAL;

    @Column(nullable = false)
    private Instant expiresAt;
}
