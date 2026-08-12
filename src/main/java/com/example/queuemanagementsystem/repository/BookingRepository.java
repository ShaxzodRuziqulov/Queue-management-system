package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.Booking;
import com.example.queuemanagementsystem.domain.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Page<Booking> findByCustomer_Id(UUID customerId, Pageable pageable);

    Page<Booking> findByBusiness_Id(UUID businessId, Pageable pageable);

    @Query("""
            SELECT b
            FROM Booking b
            LEFT JOIN b.customer customer
            LEFT JOIN b.client client
            LEFT JOIN b.offeredService offeredService
            LEFT JOIN b.staff staff
            WHERE b.business.id = :businessId
              AND (:status IS NULL OR b.status = :status)
              AND (:dayStart IS NULL OR b.startAt >= :dayStart)
              AND (:dayEnd IS NULL OR b.startAt < :dayEnd)
              AND (
                :q = '' OR
                LOWER(COALESCE(b.guestName, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(b.guestPhone, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(b.customerNote, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(client.fullName, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(client.phone, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(offeredService.name, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(staff.firstName, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(staff.lastName, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(customer.firstName, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(customer.lastName, '')) LIKE CONCAT('%', :q, '%') OR
                LOWER(COALESCE(customer.username, '')) LIKE CONCAT('%', :q, '%')
              )
            """)
    Page<Booking> searchByBusiness(
            @Param("businessId") UUID businessId,
            @Param("dayStart") Instant dayStart,
            @Param("dayEnd") Instant dayEnd,
            @Param("status") BookingStatus status,
            @Param("q") String q,
            Pageable pageable);

    Page<Booking> findByBusiness_IdAndStartAtBetween(UUID businessId, Instant start, Instant end, Pageable pageable);

    List<Booking> findByBusiness_IdAndStartAtBetween(UUID businessId, Instant start, Instant end);

    List<Booking> findByStaff_Id(UUID staffId);

    long countByStaff_Id(UUID staffId);

    long countByStaff_IdAndStatus(UUID staffId, BookingStatus status);

    @Query("""
            select case when count(b) > 0 then true else false end
            from Booking b
            where b.staff.id = :staffId
              and (:excludeId is null or b.id <> :excludeId)
              and b.status not in :excludedStatuses
              and b.startAt < :endAt
              and b.endAt > :startAt
            """)
    boolean existsOverlapping(@Param("staffId") UUID staffId,
                               @Param("startAt") Instant startAt,
                               @Param("endAt") Instant endAt,
                               @Param("excludeId") UUID excludeId,
                               @Param("excludedStatuses") Collection<BookingStatus> excludedStatuses);
}
