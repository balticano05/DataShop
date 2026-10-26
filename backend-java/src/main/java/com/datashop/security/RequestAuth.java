package com.datashop.security;

import com.datashop.web.Errors;
import io.javalin.http.Context;

import java.util.Optional;

public class RequestAuth {

    private static final String ATTRIBUTE = "datashop.auth.user";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final AuthCookies authCookies;

    public RequestAuth(JwtTokenService jwtTokenService, AuthCookies authCookies) {
        this.jwtTokenService = jwtTokenService;
        this.authCookies = authCookies;
    }

    public Optional<AuthUser> optional(Context context) {
        Object cached = context.attribute(ATTRIBUTE);
        if (cached instanceof AuthUser authUser) {
            return Optional.of(authUser);
        }
        String token = extract(context);
        Optional<AuthUser> authUser = jwtTokenService.parse(token);
        authUser.ifPresent(user -> context.attribute(ATTRIBUTE, user));
        return authUser;
    }

    public AuthUser require(Context context) {
        return optional(context).orElseThrow(() -> Errors.unauthorized("Authentication required"));
    }

    public AuthUser requireAdmin(Context context) {
        AuthUser user = require(context);
        if (!user.isAdmin()) {
            throw Errors.forbidden("Admin access required");
        }
        return user;
    }

    public void store(Context context, AuthUser user) {
        context.attribute(ATTRIBUTE, user);
    }

    private String extract(Context context) {
        String authorization = context.header("Authorization");
        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length()).trim();
        }
        if (context.path().startsWith("/api/")) {
            return null;
        }
        String cookie = context.cookie(AuthCookies.NAME);
        if (cookie != null && !cookie.isBlank()) {
            return cookie;
        }
        return null;
    }
}
