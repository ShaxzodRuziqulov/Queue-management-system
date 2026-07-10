package com.example.queuemanagementsystem.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Joriy so'rovning autentifikatsiya kontekstidan foydalanuvchi ma'lumotini o'qiydi.
 * {@link AppUserPrincipal} JWT filtrida bir marta bazadan yuklanadi va shu yerda
 * qayta ishlatiladi — har bir chaqiruvda alohida baza so'rovi yubormaydi.
 */
@Service
public class CurrentUserService {

    public UUID getCurrentUserId() {
        Authentication auth = getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUserPrincipal principal) {
            return principal.getId();
        }
        throw new IllegalStateException("Foydalanuvchi konteksti topilmadi");
    }

    public String getCurrentUsername() {
        Authentication auth = getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    public boolean isAdmin() {
        Authentication auth = getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
