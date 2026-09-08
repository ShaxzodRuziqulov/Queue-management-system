package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.SupportTicket;
import com.example.queuemanagementsystem.domain.enums.SupportSource;
import com.example.queuemanagementsystem.domain.enums.SupportTicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {
    @Query("SELECT t FROM SupportTicket t WHERE (:status IS NULL OR t.status = :status) AND (:q = '' OR LOWER(t.requesterName) LIKE CONCAT('%',:q,'%') OR LOWER(t.subject) LIKE CONCAT('%',:q,'%'))")
    Page<SupportTicket> search(@Param("status") SupportTicketStatus status, @Param("q") String q, Pageable pageable);

    Page<SupportTicket> findByRequester_Id(UUID userId, Pageable pageable);

    Optional<SupportTicket> findFirstByTelegramChatIdAndStatusInOrderByUpdatedAtDesc(Long chatId, Collection<SupportTicketStatus> statuses);

    Optional<SupportTicket> findFirstByTelegramChatIdAndSourceAndStatusInOrderByUpdatedAtDesc(Long chatId, SupportSource source, Collection<SupportTicketStatus> statuses);

    Optional<SupportTicket> findFirstByTelegramChatIdOrderByUpdatedAtDesc(Long chatId);

    Optional<SupportTicket> findFirstByTelegramChatIdAndSourceOrderByUpdatedAtDesc(Long chatId, SupportSource source);
}
