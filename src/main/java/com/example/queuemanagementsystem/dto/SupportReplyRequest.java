package com.example.queuemanagementsystem.dto;
import jakarta.validation.constraints.NotBlank;
public record SupportReplyRequest(@NotBlank(message = "Javob bo'sh bo'lmasligi kerak") String content) {}
