package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.Business;
import com.example.queuemanagementsystem.domain.enums.BusinessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {

    Page<Business> findByOwner_Id(UUID ownerId, Pageable pageable);

    boolean existsByOwner_Id(UUID ownerId);

    /**
     * Muddati tugagan TRIAL bizneslarni birdaniga EXPIRED qilib yangilaydi (bulk update).
     */
    @Modifying
    @Query("UPDATE Business b SET b.status = :expiredStatus WHERE b.status = :trialStatus AND b.trialEndDate < :now")
    int expireTrials(BusinessStatus trialStatus, BusinessStatus expiredStatus, Instant now);

    /**
     * Obuna muddati tugagan ACTIVE bizneslarni birdaniga EXPIRED qilib yangilaydi (bulk update).
     * subscriptionEndDate = NULL bo'lganlar (cheksiz faol) tegilmaydi.
     */
    @Modifying
    @Query("UPDATE Business b SET b.status = :expiredStatus " +
            "WHERE b.status = :activeStatus AND b.subscriptionEndDate IS NOT NULL AND b.subscriptionEndDate < :now")
    int expireLapsedSubscriptions(BusinessStatus activeStatus, BusinessStatus expiredStatus, Instant now);
}
