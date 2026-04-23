package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {

    List<Business> findByOwner_Id(UUID ownerId);

    boolean existsByOwner_Id(UUID ownerId);
}
