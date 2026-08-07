package com.example.queuemanagementsystem.web.error;

import com.example.queuemanagementsystem.exception.BusinessAccessDeniedException;
import com.example.queuemanagementsystem.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.UUID;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> staticResourceNotFound(NoResourceFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "Resurs topilmadi", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> dataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        String errorId = errorId();
        log.warn("[{}] Data integrity violation at {} {}", errorId, request.getMethod(), request.getRequestURI(), ex);
        return error(HttpStatus.CONFLICT, "Ma'lumotlar bazasi cheklovi buzildi", request, errorId);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("Validatsiya xatosi");
        return error(HttpStatus.BAD_REQUEST, msg, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> malformedBody(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed request body at {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return error(HttpStatus.BAD_REQUEST, "So'rov ma'lumotlari noto'g'ri formatda", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> badRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> forbidden(AccessDeniedException ex, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "Ruxsat yo'q", request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> illegalState(IllegalStateException ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> paymentRequired(BusinessAccessDeniedException ex, HttpServletRequest request) {
        return error(HttpStatus.PAYMENT_REQUIRED, ex.getMessage(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> badCredentials(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Authentication failed at {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return error(HttpStatus.UNAUTHORIZED, "Login yoki parol noto'g'ri", request);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> disabled(DisabledException ex, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "Hisob faol emas", request);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> locked(LockedException ex, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "Hisob bloklangan", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> authenticationFailed(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication error at {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return error(HttpStatus.UNAUTHORIZED, "Autentifikatsiya amalga oshmadi", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unexpected(Exception ex, HttpServletRequest request) {
        String errorId = errorId();
        log.error("[{}] Unexpected error at {} {}", errorId, request.getMethod(), request.getRequestURI(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Kutilmagan xatolik yuz berdi", request, errorId);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, HttpServletRequest request) {
        return error(status, message, request, null);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, HttpServletRequest request, String errorId) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .message(message)
                        .status(status.value())
                        .path(request.getRequestURI())
                        .errorId(errorId)
                        .timestamp(Instant.now())
                        .build());
    }

    private String errorId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
