package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.domain.PasswordResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, UUID> {
    Optional<PasswordResetCode> findTopByUserOrderByCreatedAtDesc(AppUser user);
}
