package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.Booking;
import com.example.queuemanagementsystem.domain.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {

    Page<Booking> findByCustomer_Id(UUID customerId, Pageable pageable);

    Page<Booking> findByBusiness_Id(UUID businessId, Pageable pageable);

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
