package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.domain.PasswordResetCode;
import com.example.queuemanagementsystem.dto.PasswordResetConfirmRequest;
import com.example.queuemanagementsystem.dto.PasswordResetRequest;
import com.example.queuemanagementsystem.repository.AppUserRepository;
import com.example.queuemanagementsystem.repository.PasswordResetCodeRepository;
import com.example.queuemanagementsystem.security.AppUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PasswordResetService {

    private final PasswordEncoder passwordEncoder;
    private final PasswordResetCodeRepository resetCodeRepository;

    private final SecureRandom secureRandom = new SecureRandom();
    private final AppUserRepository appUserRepository;
    private final EmailService emailService;

    public void requestReset(PasswordResetRequest request) {
        String username = AppUserDetailsService.normalizeLogin(request.getLogin());
        log.info("Password reset requested for login={}", username);

        appUserRepository.findByUsername(username).ifPresent(user -> {
            if (!StringUtils.hasText(user.getEmail())) {
                log.warn("Password reset skipped for login={}: user has no email", username);
                return;
            }

            String code = generateCode();

            PasswordResetCode resetCode = new PasswordResetCode();
            resetCode.setUser(user);
            resetCode.setCodeHash(passwordEncoder.encode(code));
            resetCode.setExpiresAt(LocalDateTime.now().plusMinutes(10));
            resetCodeRepository.save(resetCode);
            log.info("Password reset code saved for login={}, email={}", username, maskEmail(user.getEmail()));

            emailService.sendPasswordResetCode(user.getEmail(), code);
            log.info("Password reset email sent for login={}, email={}", username, maskEmail(user.getEmail()));
        });

    }

    public void confirmReset(PasswordResetConfirmRequest request) {
        String username = AppUserDetailsService.normalizeLogin(request.getLogin());
        log.info("Password reset confirmation requested for login={}", username);

        AppUser user = appUserRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Kod noto'g'ri yoki muddati o'tgan"));

        PasswordResetCode resetCode = resetCodeRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new IllegalArgumentException("Kod noto'g'ri yoki muddati o'tgan"));

        if (resetCode.isUsed() || resetCode.isExpired()) {
            log.warn("Password reset confirmation rejected for login={}: code used or expired", username);
            throw new IllegalArgumentException("Kod noto'g'ri yoki muddati o'tgan");
        }

        if (resetCode.getAttemptCount() >= 5) {
            log.warn("Password reset confirmation rejected for login={}: too many attempts", username);
            throw new IllegalArgumentException("Urinishlar soni tugagan");
        }

        resetCode.setAttemptCount(resetCode.getAttemptCount() + 1);

        if (!passwordEncoder.matches(request.getCode(), resetCode.getCodeHash())) {
            resetCodeRepository.save(resetCode);
            log.warn("Password reset confirmation rejected for login={}: invalid code attempt={}", username, resetCode.getAttemptCount());
            throw new IllegalArgumentException("Kod noto'g'ri yoki muddati o'tgan");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        resetCode.setUsedAt(LocalDateTime.now());

        appUserRepository.save(user);
        resetCodeRepository.save(resetCode);
        log.info("Password reset completed for login={}", username);
    }

    private String generateCode() {
        int number = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(number);
    }

    private String maskEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return "";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***" + email.substring(Math.max(atIndex, 0));
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}
