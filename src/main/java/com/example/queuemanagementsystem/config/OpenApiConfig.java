package com.example.queuemanagementsystem.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI hujjatlari sozlamasi.
 * "Authorize" tugmasi orqali JWT (Bearer) token kiritib, himoyalangan
 * endpointlarni bevosita Swagger UI'dan sinash mumkin.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Queue Management System API", version = "v1",
                description = "Navbat boshqaruv tizimi uchun REST API hujjatlari"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
