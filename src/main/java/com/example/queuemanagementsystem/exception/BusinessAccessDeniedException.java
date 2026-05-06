package com.example.queuemanagementsystem.exception;

/**
 * Biznes trial muddati tugagan yoki obuna faol bo'lmaganda otiladi.
 * HTTP 402 Payment Required qaytaradi.
 */
public class BusinessAccessDeniedException extends RuntimeException {

    public BusinessAccessDeniedException(String message) {
        super(message);
    }
}
