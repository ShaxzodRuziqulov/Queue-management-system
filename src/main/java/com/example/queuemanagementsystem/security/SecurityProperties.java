package com.example.queuemanagementsystem.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();

    @Data
    public static class Jwt {
        private String secret = "";
        private long expirationMs = 86_400_000L;
    }

    @Data
    public static class Cors {
        private List<String> allowedOrigins = List.of("*");
    }
}
