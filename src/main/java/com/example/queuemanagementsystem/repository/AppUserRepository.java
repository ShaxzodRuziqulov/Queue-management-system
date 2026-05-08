package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM AppUser u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<AppUser> findWithRolesByUsername(@Param("username") String username);

    @Query("SELECT DISTINCT u FROM AppUser u LEFT JOIN FETCH u.roles")
    List<AppUser> findAllWithRoles();
}
