package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.domain.PasswordResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {
    Optional<PasswordResetCode> findTopByUserOrderByCreatedAtDesc(AppUser user);
}
