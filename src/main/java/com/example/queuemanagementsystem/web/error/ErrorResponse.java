package com.example.queuemanagementsystem.web.error;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ErrorResponse {
    String message;
}
