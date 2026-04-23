package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, UUID> {

    List<UserIdentity> findByUser_Id(UUID userId);

    Optional<UserIdentity> findByUser_IdAndId(UUID userId, UUID id);
}
