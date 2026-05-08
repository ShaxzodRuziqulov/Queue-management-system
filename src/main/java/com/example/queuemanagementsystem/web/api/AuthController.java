package com.example.queuemanagementsystem.web.api;

import com.example.queuemanagementsystem.dto.auth.LoginRequest;
import com.example.queuemanagementsystem.dto.auth.LoginResponse;
import com.example.queuemanagementsystem.dto.auth.RegisterRequest;
import com.example.queuemanagementsystem.security.AppUserDetailsService;
import com.example.queuemanagementsystem.security.AppUserPrincipal;
import com.example.queuemanagementsystem.security.JwtService;
import com.example.queuemanagementsystem.service.AppUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AppUserService appUserService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String username = AppUserDetailsService.normalizeLogin(request.getLogin());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword()));
        return ResponseEntity.ok(buildLoginResponse(authentication));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        appUserService.register(request);
        String username = AppUserDetailsService.normalizeLogin(request.getLogin());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword()));
        return ResponseEntity.status(HttpStatus.CREATED).body(buildLoginResponse(authentication));
    }

    private LoginResponse buildLoginResponse(Authentication authentication) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(principal);
        long expiresInSeconds = TimeUnit.MILLISECONDS.toSeconds(jwtService.getExpirationTime());
        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresInSeconds(expiresInSeconds)
                .userId(principal.getId())
                .login(principal.getUsername())
                .displayName(principal.getDisplayName())
                .avatarUrl(principal.getAvatarUrl())
                .businessOwner(principal.isBusinessOwner())
                .admin(principal.isAdmin())
                .roles(roles)
                .build();
    }
}
