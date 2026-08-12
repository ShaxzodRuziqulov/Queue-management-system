package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.AuditLog;
import com.example.queuemanagementsystem.domain.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    @Query("""
            SELECT a
            FROM AuditLog a
            WHERE (:entityType = '' OR a.entityType = :entityType)
              AND (:action IS NULL OR a.action = :action)
              AND (:adminLogin = '' OR LOWER(a.adminLogin) LIKE CONCAT('%', :adminLogin, '%'))
            """)
    Page<AuditLog> search(
            @Param("entityType") String entityType,
            @Param("action") AuditAction action,
            @Param("adminLogin") String adminLogin,
            Pageable pageable);
}
