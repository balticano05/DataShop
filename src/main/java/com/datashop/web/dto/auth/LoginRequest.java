package com.datashop.web.dto.auth;

import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public record LoginRequest(
        @OpenApiRequired
        @OpenApiExample("customer@example.com")
        String email,
        @OpenApiRequired
        @OpenApiExample("Password123")
        String password
) {
}
