package com.datashop.security;

import com.datashop.config.AppConfig;
import com.datashop.web.Errors;
import io.javalin.http.Context;
import io.javalin.http.Cookie;
import io.javalin.http.SameSite;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class CsrfTokens {

    public static final String COOKIE = "datashop_csrf";
    public static final String FORM_FIELD = "_csrf";
    public static final String HEADER = "X-CSRF-Token";

    private static final String ATTRIBUTE = "datashop.csrf";

    private final SecureRandom random = new SecureRandom();
    private final AppConfig config;

    public CsrfTokens(AppConfig config) {
        this.config = config;
    }

    public String token(Context context) {
        Object cached = context.attribute(ATTRIBUTE);
        if (cached instanceof String value) {
            return value;
        }
        String existing = context.cookie(COOKIE);
        String token = existing == null || existing.isBlank() ? generate() : existing;
        Cookie cookie = new Cookie(COOKIE, token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(config.isSecureCookies());
        cookie.setSameSite(SameSite.LAX);
        cookie.setMaxAge(maxAgeSeconds());
        context.cookie(cookie);
        context.attribute(ATTRIBUTE, token);
        return token;
    }

    public void verify(Context context) {
        String expected = context.cookie(COOKIE);
        String actual = context.formParam(FORM_FIELD);
        if (actual == null || actual.isBlank()) {
            actual = context.header(HEADER);
        }
        if (expected == null || actual == null || !constantTimeEquals(expected, actual)) {
            throw Errors.forbidden("Invalid CSRF token");
        }
    }

    private String generate() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean constantTimeEquals(String left, String right) {
        return MessageDigest.isEqual(
                left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8)
        );
    }

    private int maxAgeSeconds() {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, config.getJwtTtl().toSeconds()));
    }
}
