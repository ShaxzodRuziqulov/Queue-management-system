package com.example.queuemanagementsystem.repository;
import com.example.queuemanagementsystem.domain.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface SupportMessageRepository extends JpaRepository<SupportMessage, UUID> { List<SupportMessage> findByTicket_IdOrderByCreatedAtAsc(UUID ticketId); }
