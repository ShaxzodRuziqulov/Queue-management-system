package com.example.queuemanagementsystem.repository;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Role findByName(String name);

    /** Rol mavjud bo'lsa, foydalanuvchiga biriktiradi (mavjud bo'lmasa jim o'tkazib yuboradi). */
    default void assignRoleIfPresent(AppUser user, String roleName) {
        Role role = findByName(roleName);
        if (role != null) user.getRoles().add(role);
    }

    /** Rol mavjud bo'lsa, foydalanuvchidan olib tashlaydi. */
    default void removeRoleIfPresent(AppUser user, String roleName) {
        Role role = findByName(roleName);
        if (role != null) user.getRoles().remove(role);
    }
}
