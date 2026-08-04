package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.StaffMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffMemberRepository extends JpaRepository<StaffMember, UUID> {

    List<StaffMember> findByBusiness_Id(UUID businessId);

    @org.springframework.data.jpa.repository.Query("""
            select distinct s
            from StaffMember s
            join s.offeredServices service
            where s.business.id = :businessId
              and service.id = :serviceId
              and s.active = true
            """)
    List<StaffMember> findActiveByBusinessIdAndServiceId(UUID businessId, UUID serviceId);

    Optional<StaffMember> findByBusiness_IdAndId(UUID businessId, UUID id);

    Optional<StaffMember> findByLinkedUser_Id(UUID userId);

    boolean existsByBusiness_IdAndLinkedUser_Id(UUID businessId, UUID linkedUserId);

    boolean existsByBusiness_IdAndIdAndOfferedServices_Id(UUID businessId, UUID id, UUID serviceId);
}
