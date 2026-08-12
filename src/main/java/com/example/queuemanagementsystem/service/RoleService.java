package com.example.queuemanagementsystem.service;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.domain.Role;
import com.example.queuemanagementsystem.domain.enums.RoleName;
import com.example.queuemanagementsystem.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository repository;

    public Role requireRole(RoleName roleName) {
        return requireRole(roleName.name());
    }

    public Role requireRole(String roleName) {
        return repository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role topilmadi: " + roleName));
    }

    public void assignRole(AppUser user, RoleName roleName) {
        user.getRoles().add(requireRole(roleName));
    }

    public void removeRole(AppUser user, RoleName roleName) {
        user.getRoles().remove(requireRole(roleName));
    }
}
