package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.StaffMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffMemberRepository extends JpaRepository<StaffMember, UUID> {

    List<StaffMember> findByBusiness_Id(UUID businessId);

    Optional<StaffMember> findByBusiness_IdAndId(UUID businessId, UUID id);

    Optional<StaffMember> findByLinkedUser_Id(UUID userId);

    boolean existsByLinkedUser_Id(UUID userId);
}
