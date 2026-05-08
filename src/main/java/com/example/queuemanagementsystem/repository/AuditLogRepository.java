package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    @Query("""
            SELECT a FROM AuditLog a
            WHERE (:entityType IS NULL OR a.entityType = :entityType)
              AND (:action IS NULL OR a.action = :action)
              AND (:adminLogin IS NULL OR LOWER(a.adminLogin) LIKE LOWER(CONCAT('%', :adminLogin, '%')))
            """)
    Page<AuditLog> findFiltered(
            @Param("entityType") String entityType,
            @Param("action") String action,
            @Param("adminLogin") String adminLogin,
            Pageable pageable);
}
