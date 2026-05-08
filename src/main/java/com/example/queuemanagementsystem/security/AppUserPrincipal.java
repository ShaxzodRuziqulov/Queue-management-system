package com.example.queuemanagementsystem.security;

import com.example.queuemanagementsystem.domain.AppUser;
import com.example.queuemanagementsystem.domain.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AppUserPrincipal implements UserDetails {

    @Getter private final UUID id;
    @Getter private final String displayName;
    @Getter private final String avatarUrl;
    private final String username;
    private final String passwordHash;
    private final boolean active;
    private final List<GrantedAuthority> authorities;

    public AppUserPrincipal(AppUser user) {
        this.id          = user.getId();
        this.username    = user.getUsername();
        this.displayName = user.getDisplayName();
        this.avatarUrl   = user.getAvatarUrl();
        this.passwordHash = user.getPasswordHash();
        this.active      = user.isActive();
        this.authorities = user.getRoles().stream()
                .map(Role::getName)
                .map(SimpleGrantedAuthority::new)
                .map(a -> (GrantedAuthority) a)
                .toList();
    }

    public boolean isAdmin() {
        return authorities.stream().anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));
    }

    public boolean isBusinessOwner() {
        return authorities.stream().anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_BUSINESS_OWNER"));
    }

    @Override public String getUsername() { return username; }
    @Override public String getPassword() { return passwordHash; }
    @Override public boolean isEnabled() { return active; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
}
