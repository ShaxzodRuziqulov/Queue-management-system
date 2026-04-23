package com.example.queuemanagementsystem.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CurrentUserService {

    public Optional<AppUserPrincipal> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof AppUserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public UUID requireUserId() {
        return currentUser().map(AppUserPrincipal::getId)
                .orElseThrow(() -> new IllegalStateException("Foydalanuvchi konteksti topilmadi"));
    }

    public boolean isAdmin() {
        return currentUser().map(AppUserPrincipal::isAdmin).orElse(false);
    }
}
