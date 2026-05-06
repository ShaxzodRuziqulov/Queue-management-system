package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.Business;
import com.example.queuemanagementsystem.domain.enums.BusinessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {

    List<Business> findByOwner_Id(UUID ownerId);

    boolean existsByOwner_Id(UUID ownerId);

    /**
     * Trial muddati o'tib ketgan, hali EXPIRED qilinmagan bizneslarni topadi.
     */
    @Query("SELECT b FROM Business b WHERE b.status = :trialStatus AND b.trialEndDate < :now")
    List<Business> findExpiredTrials(BusinessStatus trialStatus, Instant now);

    /**
     * Muddati tugagan TRIAL bizneslarni birdaniga EXPIRED qilib yangilaydi (bulk update).
     */
    @Modifying
    @Query("UPDATE Business b SET b.status = :expiredStatus WHERE b.status = :trialStatus AND b.trialEndDate < :now")
    int expireTrials(BusinessStatus trialStatus, BusinessStatus expiredStatus, Instant now);
}
