package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.OfferedService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OfferedServiceRepository extends JpaRepository<OfferedService, UUID> {

    List<OfferedService> findByBusiness_Id(UUID businessId);

    List<OfferedService> findByBusiness_IdAndIdIn(UUID business_id, Collection<UUID> id);

    Optional<OfferedService> findByBusiness_IdAndId(UUID businessId, UUID id);
}
