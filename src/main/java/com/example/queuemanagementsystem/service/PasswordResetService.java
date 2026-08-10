package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.domain.PasswordResetCode;
import com.example.queuemanagementsystem.dto.PasswordResetConfirmRequest;
import com.example.queuemanagementsystem.dto.PasswordResetRequest;
import com.example.queuemanagementsystem.repository.AppUserRepository;
import com.example.queuemanagementsystem.repository.PasswordResetCodeRepository;
import com.example.queuemanagementsystem.security.AppUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final AppUserService appUserService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetCodeRepository resetCodeRepository;

    private final SecureRandom secureRandom = new SecureRandom();
    private final AppUserRepository appUserRepository;
    private final EmailService emailService;

    public void requestReset(PasswordResetRequest request) {
        String username = AppUserDetailsService.normalizeLogin(request.getLogin());

        appUserRepository.findByUsername(username).ifPresent(user -> {
            if (!StringUtils.hasText(user.getEmail())) {
                return;
            }

            String code = generateCode();

            PasswordResetCode resetCode = new PasswordResetCode();
            resetCode.setUser(user);
            resetCode.setCodeHash(passwordEncoder.encode(code));
            resetCode.setExpiresAt(LocalDateTime.now().plusSeconds(10));
            resetCodeRepository.save(resetCode);

            emailService.sendPasswordResetCode(user.getEmail(), code);
        });

    }

    public void confirmReset(PasswordResetConfirmRequest request) {
        String username = AppUserDetailsService.normalizeLogin(request.getLogin());

        AppUser user = appUserRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Kod noto'g'ri yoki muddati o'tgan"));

        PasswordResetCode resetCode = resetCodeRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new IllegalArgumentException("Kod noto'g'ri yoki muddati o'tgan"));

        if (resetCode.isUsed() || resetCode.isExpired()) {
            throw new IllegalArgumentException("Kod noto'g'ri yoki muddati o'tgan");
        }

        if (resetCode.getAttemptCount() >= 5) {
            throw new IllegalArgumentException("Urinishlar soni tugagan");
        }

        resetCode.setAttemptCount(resetCode.getAttemptCount() + 1);

        if (!passwordEncoder.matches(request.getCode(), resetCode.getCodeHash())) {
            resetCodeRepository.save(resetCode);
            throw new IllegalArgumentException("Kod noto'g'ri yoki muddati o'tgan");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        resetCode.setUsedAt(LocalDateTime.now());

        appUserRepository.save(user);
        resetCodeRepository.save(resetCode);
    }

    private String generateCode() {
        int number = 100000 + secureRandom.nextInt(99999);
        return String.valueOf(number);
    }
}
