package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByBooking_Id(UUID bookingId);

    Optional<Review> findByBooking_Id(UUID bookingId);

    List<Review> findByBooking_Business_Id(UUID businessId);

    List<Review> findByStaff_Id(UUID staffId);

    /** Xodimning o'rtacha reytingi */
    @org.springframework.data.jpa.repository.Query(
        "SELECT COALESCE(AVG(r.stars), 0) FROM Review r WHERE r.staff.id = :staffId")
    double avgStarsByStaffId(@org.springframework.data.repository.query.Param("staffId") UUID staffId);
}
