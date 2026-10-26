package com.datashop.security;

import com.datashop.config.AppConfig;
import io.javalin.http.Context;

public class SecurityHeaders {

    private static final String CSP = String.join("; ",
            "default-src 'self'",
            "img-src 'self' https: data:",
            "style-src 'self' 'unsafe-inline'",
            "script-src 'self'",
            "font-src 'self' data:",
            "connect-src 'self'",
            "frame-ancestors 'none'",
            "base-uri 'self'",
            "form-action 'self'"
    );

    private final AppConfig config;

    public SecurityHeaders(AppConfig config) {
        this.config = config;
    }

    public void apply(Context context) {
        context.header("X-Content-Type-Options", "nosniff");
        context.header("X-Frame-Options", "DENY");
        context.header("Referrer-Policy", "strict-origin-when-cross-origin");
        context.header("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        context.header("Content-Security-Policy", CSP);
        if (config.isSecureCookies()) {
            context.header("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
        if (context.path().startsWith("/api/")) {
            context.header("Cache-Control", "no-store");
        }
    }
}
