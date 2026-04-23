package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByCustomer_Id(UUID customerId);

    List<Booking> findByBusiness_Id(UUID businessId);
}
