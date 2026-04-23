package com.example.queuemanagementsystem.config;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.repository.AppUserRepository;
import com.example.queuemanagementsystem.security.AppUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(0)
@RequiredArgsConstructor
public class AdminUserBootstrap implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.admin-login:admin}")
    private String adminLogin;

    @Value("${app.security.admin-password:admin}")
    private String adminPassword;

    @Value("${app.security.admin-bootstrap:true}")
    private boolean enabled;

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        String login = AppUserDetailsService.normalizeLogin(adminLogin);
        if (login.isEmpty() || appUserRepository.existsByLogin(login)) {
            return;
        }
        AppUser admin = new AppUser();
        admin.setLogin(login);
        admin.setDisplayName("Administrator");
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setActive(true);
        appUserRepository.save(admin);
    }
}
