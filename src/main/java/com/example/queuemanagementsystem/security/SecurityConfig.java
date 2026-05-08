package com.example.queuemanagementsystem.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RestAuthHandlers restAuthHandlers;

    @Value("${app.security.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // ── ADMIN only ───────────────────────────────────────────
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/v1/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/api/v1/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/businesses/*/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/api/v1/businesses/*/review").hasRole("ADMIN")

                        // ── Business CRUD (owner creates/edits/deletes own business) ──
                        // ROLE_USER ham biznes yarata olishi kerak (birinchi marta, onboarding)
                        .requestMatchers(HttpMethod.POST,   "/api/v1/businesses").hasAnyRole("USER", "BUSINESS_OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/businesses/*").hasAnyRole("BUSINESS_OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/businesses/*").hasAnyRole("BUSINESS_OWNER", "ADMIN")

                        // ── Staff portal (o'z profili) ───────────────────────────
                        .requestMatchers("/api/v1/staff/me/**").hasAnyRole("STAFF", "BUSINESS_OWNER", "MANAGER", "ADMIN")

                        // ── Staff management ─────────────────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/v1/businesses/*/staff").hasAnyRole("BUSINESS_OWNER", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/businesses/*/staff/*").hasAnyRole("BUSINESS_OWNER", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/businesses/*/staff/*").hasAnyRole("BUSINESS_OWNER", "MANAGER", "ADMIN")

                        // ── Offered services management ──────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/v1/businesses/*/services").hasAnyRole("BUSINESS_OWNER", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/businesses/*/services/*").hasAnyRole("BUSINESS_OWNER", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/businesses/*/services/*").hasAnyRole("BUSINESS_OWNER", "MANAGER", "ADMIN")

                        // ── Business hours management ────────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/v1/businesses/*/hours").hasAnyRole("BUSINESS_OWNER", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/businesses/*/hours/*").hasAnyRole("BUSINESS_OWNER", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/businesses/*/hours/*").hasAnyRole("BUSINESS_OWNER", "MANAGER", "ADMIN")

                        // ── Everything else: any authenticated user ──────────────
                        .anyRequest().authenticated())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthHandlers.authenticationEntryPoint())
                        .accessDeniedHandler(restAuthHandlers.accessDeniedHandler()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(o -> !o.isBlank())
                .toList();

        boolean isWildcard = origins.contains("*");
        if (isWildcard) {
            configuration.setAllowedOriginPatterns(List.of("*"));
        } else {
            configuration.setAllowedOrigins(origins);
            configuration.setAllowCredentials(true);
        }

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept",
                "X-Requested-With", "Origin", "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
