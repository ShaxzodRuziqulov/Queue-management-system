package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.domain.PasswordResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, UUID> {
    Optional<PasswordResetCode> findTopByUserOrderByCreatedAtDesc(AppUser user);

    @Modifying
    @Query("DELETE FROM PasswordResetCode p WHERE p.user.id = :userId")
    int deleteByUserId(@Param("userId") UUID userId);
}
