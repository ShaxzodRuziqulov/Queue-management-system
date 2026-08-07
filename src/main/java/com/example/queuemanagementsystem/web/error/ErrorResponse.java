package com.example.queuemanagementsystem.web.error;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class ErrorResponse {
    String message;
    int status;
    String path;
    String errorId;
    Instant timestamp;
}
