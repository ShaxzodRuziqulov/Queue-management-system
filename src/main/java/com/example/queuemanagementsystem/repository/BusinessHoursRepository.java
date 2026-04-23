package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.BusinessHours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusinessHoursRepository extends JpaRepository<BusinessHours, UUID> {

    List<BusinessHours> findByBusiness_IdOrderByWeekdayAsc(UUID businessId);

    Optional<BusinessHours> findByBusiness_IdAndId(UUID businessId, UUID id);
}
