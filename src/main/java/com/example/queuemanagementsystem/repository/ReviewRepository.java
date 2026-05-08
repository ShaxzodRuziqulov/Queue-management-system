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
}
