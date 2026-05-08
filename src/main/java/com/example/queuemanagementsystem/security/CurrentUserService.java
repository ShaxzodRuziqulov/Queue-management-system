package com.example.queuemanagementsystem.security;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final AppUserRepository repository;

    public AppUser getCurrentUser() {
        String username = getCurrentUsername();
        return repository.findWithRolesByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Foydalanuvchi konteksti topilmadi"));
    }

    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /** BusinessService va AppUserService eski kodi bilan moslik uchun */
    public UUID requireUserId() {
        return getCurrentUserId();
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
