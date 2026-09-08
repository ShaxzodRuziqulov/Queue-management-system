package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.TelegramLinkToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TelegramLinkTokenRepository extends JpaRepository<TelegramLinkToken, UUID> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    Optional<TelegramLinkToken> findByToken(String token);
    void deleteByUser_Id(UUID userId);
}
