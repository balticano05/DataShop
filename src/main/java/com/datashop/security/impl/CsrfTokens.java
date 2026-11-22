package com.datashop.security.impl;

import com.datashop.config.AppConfig;
import com.datashop.security.CsrfService;
import com.datashop.util.Constants;
import com.datashop.util.web.Errors;
import io.javalin.http.Context;
import io.javalin.http.Cookie;
import io.javalin.http.SameSite;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@RequiredArgsConstructor
public class CsrfTokens implements CsrfService {

    private final SecureRandom random = new SecureRandom();
    private final AppConfig config;

    @Override
    public String token(Context context) {
        Object cached = context.attribute(Constants.Security.CSRF_ATTRIBUTE);
        if (cached instanceof String value) {
            return value;
        }
        String existing = context.cookie(Constants.Security.CSRF_COOKIE);
        String token = existing == null || existing.isBlank() ? generate() : existing;
        Cookie cookie = new Cookie(Constants.Security.CSRF_COOKIE, token);
        cookie.setPath(Constants.Security.COOKIE_PATH);
        cookie.setHttpOnly(true);
        cookie.setSecure(config.isSecureCookies());
        cookie.setSameSite(SameSite.LAX);
        cookie.setMaxAge(maxAgeSeconds());
        context.cookie(cookie);
        context.attribute(Constants.Security.CSRF_ATTRIBUTE, token);
        return token;
    }

    @Override
    public void verify(Context context) {
        String expected = context.cookie(Constants.Security.CSRF_COOKIE);
        String actual = context.formParam(Constants.Security.CSRF_FORM_FIELD);
        if (actual == null || actual.isBlank()) {
            actual = context.header(Constants.Security.CSRF_HEADER);
        }
        if (expected == null || actual == null || !constantTimeEquals(expected, actual)) {
            throw Errors.forbidden(Constants.Messages.INVALID_CSRF);
        }
    }

    private String generate() {
        byte[] bytes = new byte[Constants.Security.CSRF_TOKEN_BYTES];
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
