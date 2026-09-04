package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.StaffMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffMemberRepository extends JpaRepository<StaffMember, UUID> {

    List<StaffMember> findByBusiness_Id(UUID businessId);

    @Modifying
    @Query(value = """
            DELETE FROM staff_member_services
            WHERE staff_id IN (SELECT id FROM staff_members WHERE business_id = :businessId)
               OR service_id IN (SELECT id FROM offered_services WHERE business_id = :businessId)
            """, nativeQuery = true)
    int deleteServiceLinksByBusinessId(@Param("businessId") UUID businessId);

    @Modifying
    @Query("DELETE FROM StaffMember s WHERE s.business.id = :businessId")
    int deleteByBusinessId(@Param("businessId") UUID businessId);

    @Modifying
    @Query("UPDATE StaffMember s SET s.linkedUser = null WHERE s.linkedUser.id = :userId")
    int clearLinkedUser(@Param("userId") UUID userId);

    List<StaffMember> findDistinctByBusiness_IdAndOfferedServices_IdAndActiveTrue(UUID businessId, UUID serviceId);

    Optional<StaffMember> findByBusiness_IdAndId(UUID businessId, UUID id);

    Optional<StaffMember> findByLinkedUser_Id(UUID userId);

    boolean existsByBusiness_IdAndLinkedUser_Id(UUID businessId, UUID linkedUserId);

    boolean existsByBusiness_IdAndIdAndOfferedServices_Id(UUID businessId, UUID id, UUID serviceId);
}
