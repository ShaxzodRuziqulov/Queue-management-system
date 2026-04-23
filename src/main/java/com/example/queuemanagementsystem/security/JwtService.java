package com.example.queuemanagementsystem.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final SecurityProperties securityProperties;

    public String createToken(Authentication authentication) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(securityProperties.getJwt().getExpirationMs());
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        var builder = Jwts.builder()
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp));
        if (authentication.getPrincipal() instanceof AppUserPrincipal principal) {
            builder.subject(principal.getLogin() != null ? principal.getLogin() : authentication.getName())
                    .claim("userId", principal.getId().toString())
                    .claim("businessOwner", principal.isBusinessOwner());
        } else {
            builder.subject(authentication.getName());
        }
        return builder.signWith(signingKey()).compact();
    }

    public String parseSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        byte[] bytes = securityProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            try {
                bytes = MessageDigest.getInstance("SHA-256").digest(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}
