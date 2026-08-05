package com.example.queuemanagementsystem.config;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.domain.Role;
import com.example.queuemanagementsystem.repository.AppUserRepository;
import com.example.queuemanagementsystem.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PreInject {

    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AppUserRepository userRepository;

    @PostConstruct
    @Transactional
    public void setDefaultUsers() {
        if (roleRepository.count() == 0) {
            roleRepository.saveAll(List.of(
                    Role.builder().name("ROLE_ADMIN").description("Admin").build(),
                    Role.builder().name("ROLE_MANAGER").description("Manager").build(),
                    Role.builder().name("ROLE_BUSINESS_OWNER").description("Business Owner").build(),
                    Role.builder().name("ROLE_STAFF").description("Staff Member").build(),
                    Role.builder().name("ROLE_USER").description("User").build()
            ));
        }

        if (userRepository.count() == 0) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setFirstName("first_name");
            admin.setLastName("last_name");
            admin.setPhone("123456789");
            admin.setActive(true);
            admin.setPasswordHash(passwordEncoder.encode("1234"));
            admin.setRoles(Set.of(roleRepository.findByName("ROLE_ADMIN")));
            userRepository.save(admin);
        }

        backfillMissingNames();
    }

    /**
     * Eski yozuvlarda firstName bo'sh qolgan bo'lsa, username'dan to'ldiradi.
     */
    private void backfillMissingNames() {
        List<AppUser> missing = userRepository.findAll().stream()
                .filter(u -> !StringUtils.hasText(u.getFirstName()))
                .toList();
        if (missing.isEmpty()) return;
        for (AppUser u : missing) {
            u.setFirstName(u.getUsername());
        }
        userRepository.saveAll(missing);
    }
}

