package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.Business;
import com.example.queuemanagementsystem.domain.enums.BusinessCategory;
import com.example.queuemanagementsystem.domain.enums.BusinessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {

    @Query("""
            SELECT DISTINCT b.city
            FROM Business b
            WHERE b.city IS NOT NULL AND b.city <> ''
            ORDER BY b.city
            """)
    List<String> findDistinctCities();

    @Query("""
            SELECT DISTINCT b
            FROM Business b
            LEFT JOIN b.offeredServices os
            WHERE (:ownerId IS NULL OR b.owner.id = :ownerId)
              AND (:category IS NULL OR b.category = :category)
              AND (:status IS NULL OR b.status = :status)
              AND (:city = '' OR LOWER(b.city) = :city)
              AND (
                :q = '' OR
                LOWER(b.name) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(b.description, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(b.addressLine, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(b.city, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(os.name, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(os.description, '')) LIKE CONCAT('%', :q, '%')
              )
            """)
    Page<Business> search(
            @Param("ownerId") UUID ownerId,
            @Param("category") BusinessCategory category,
            @Param("status") BusinessStatus status,
            @Param("city") String city,
            @Param("q") String q,
            Pageable pageable);

    @Query(value = """
            SELECT b
            FROM Business b
            LEFT JOIN b.offeredServices os
            LEFT JOIN b.bookings bk
            LEFT JOIN Review r ON r.booking = bk
            WHERE (:ownerId IS NULL OR b.owner.id = :ownerId)
              AND (:category IS NULL OR b.category = :category)
              AND (:status IS NULL OR b.status = :status)
              AND (:city = '' OR LOWER(b.city) = :city)
              AND (
                :q = '' OR
                LOWER(b.name) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(b.description, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(b.addressLine, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(b.city, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(os.name, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(os.description, '')) LIKE CONCAT('%', :q, '%')
              )
            GROUP BY b
            ORDER BY COALESCE(AVG(r.stars), 0) DESC, b.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT b)
            FROM Business b
            LEFT JOIN b.offeredServices os
            WHERE (:ownerId IS NULL OR b.owner.id = :ownerId)
              AND (:category IS NULL OR b.category = :category)
              AND (:status IS NULL OR b.status = :status)
              AND (:city = '' OR LOWER(b.city) = :city)
              AND (
                :q = '' OR
                LOWER(b.name) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(b.description, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(b.addressLine, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(b.city, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(os.name, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(os.description, '')) LIKE CONCAT('%', :q, '%')
              )
            """)
    Page<Business> searchOrderByRating(
            @Param("ownerId") UUID ownerId,
            @Param("category") BusinessCategory category,
            @Param("status") BusinessStatus status,
            @Param("city") String city,
            @Param("q") String q,
            Pageable pageable);

    @Query(value = """
            SELECT b
            FROM Business b
            LEFT JOIN b.offeredServices os
            LEFT JOIN b.bookings bk
            LEFT JOIN Review r ON r.booking = bk
            WHERE (:ownerId IS NULL OR b.owner.id = :ownerId)
              AND (:category IS NULL OR b.category = :category)
              AND (:status IS NULL OR b.status = :status)
              AND (:city = '' OR LOWER(b.city) = :city)
              AND (
                :q = '' OR
                LOWER(b.name) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(b.description, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(b.addressLine, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(b.city, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(os.name, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(os.description, '')) LIKE CONCAT('%', :q, '%')
              )
            GROUP BY b
            ORDER BY COUNT(DISTINCT r.id) DESC, b.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT b)
            FROM Business b
            LEFT JOIN b.offeredServices os
            WHERE (:ownerId IS NULL OR b.owner.id = :ownerId)
              AND (:category IS NULL OR b.category = :category)
              AND (:status IS NULL OR b.status = :status)
              AND (:city = '' OR LOWER(b.city) = :city)
              AND (
                :q = '' OR
                LOWER(b.name) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(b.description, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(b.addressLine, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(b.city, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(os.name, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(os.description, '')) LIKE CONCAT('%', :q, '%')
              )
            """)
    Page<Business> searchOrderByReviewCount(
            @Param("ownerId") UUID ownerId,
            @Param("category") BusinessCategory category,
            @Param("status") BusinessStatus status,
            @Param("city") String city,
            @Param("q") String q,
            Pageable pageable);

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
