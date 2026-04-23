package com.example.queuemanagementsystem.security;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.repository.AppUserRepository;
import com.example.queuemanagementsystem.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final BusinessRepository businessRepository;

    @Value("${app.security.admin-login:admin}")
    private String adminLogin;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String key = normalizeLogin(username);
        AppUser user = appUserRepository.findByLogin(key)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        if (!user.isActive()) {
            throw new UsernameNotFoundException(username);
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new UsernameNotFoundException("Parol o‘rnatilmagan");
        }
        boolean owner = businessRepository.existsByOwner_Id(user.getId());
        boolean admin = adminLogin.equalsIgnoreCase(user.getLogin());
        return new AppUserPrincipal(user, owner, admin);
    }

    public static String normalizeLogin(String login) {
        if (login == null) {
            return "";
        }
        return login.trim().toLowerCase(Locale.ROOT);
    }
}
