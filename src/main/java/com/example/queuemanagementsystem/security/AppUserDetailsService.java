package com.example.queuemanagementsystem.security;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository repository;

    public static String normalizeLogin(String login) {
        return login == null ? "" : login.trim().toLowerCase();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        AppUser user = repository.findWithRolesByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Foydalanuvchi topilmadi: " + username));
        if (!user.isActive()) {
            throw new DisabledException("Foydalanuvchi bloklangan");
        }
        return new AppUserPrincipal(user);
    }
}
