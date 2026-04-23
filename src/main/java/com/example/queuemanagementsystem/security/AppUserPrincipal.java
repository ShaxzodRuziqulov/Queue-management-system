package com.example.queuemanagementsystem.security;

import com.example.queuemanagementsystem.domain.AppUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class AppUserPrincipal implements UserDetails {

    private final UUID id;
    private final String login;
    private final String passwordHash;
    private final boolean businessOwner;
    private final boolean admin;
    private final boolean active;
    private final List<GrantedAuthority> authorities;

    public AppUserPrincipal(AppUser user, boolean businessOwner, boolean admin) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.passwordHash = user.getPasswordHash();
        this.businessOwner = businessOwner;
        this.admin = admin;
        this.active = user.isActive();
        this.authorities = buildAuthorities(businessOwner, admin);
    }

    private static List<GrantedAuthority> buildAuthorities(boolean businessOwner, boolean admin) {
        List<GrantedAuthority> list = new java.util.ArrayList<>();
        list.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (businessOwner) {
            list.add(new SimpleGrantedAuthority("ROLE_BUSINESS_OWNER"));
        }
        if (admin) {
            list.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return List.copyOf(list);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash != null ? passwordHash : "";
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
