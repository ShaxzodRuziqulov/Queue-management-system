package com.example.queuemanagementsystem.dto;
import java.time.Instant; import java.util.*;
public record SupportTicketDto(UUID id, String requesterName, String requesterLogin, String subject, String source, String status, String priority, Instant createdAt, Instant updatedAt, List<SupportMessageDto> messages) {}
