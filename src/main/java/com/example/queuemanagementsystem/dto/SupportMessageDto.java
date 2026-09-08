package com.example.queuemanagementsystem.dto;
import java.time.Instant; import java.util.UUID;
public record SupportMessageDto(UUID id, String sender, String content, String mediaType, Instant createdAt) {}
