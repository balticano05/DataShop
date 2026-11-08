package com.datashop.security;

import com.datashop.config.AppConfig;
import io.javalin.http.Context;
import io.javalin.http.Cookie;
import io.javalin.http.SameSite;

import java.time.Duration;

public class AuthCookies {

    public static final String NAME = "datashop_token";

    private final AppConfig config;
    private final Duration ttl;

    public AuthCookies(AppConfig config, Duration ttl) {
        this.config = config;
        this.ttl = ttl;
    }

    public void write(Context context, String token) {
        Cookie cookie = new Cookie(NAME, token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(config.isSecureCookies());
        cookie.setSameSite(SameSite.LAX);
        cookie.setMaxAge(maxAgeSeconds(ttl));
        context.cookie(cookie);
    }

    public void clear(Context context) {
        Cookie cookie = new Cookie(NAME, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(config.isSecureCookies());
        cookie.setSameSite(SameSite.LAX);
        cookie.setMaxAge(0);
        context.cookie(cookie);
    }

    private int maxAgeSeconds(Duration duration) {
        long seconds = duration.toSeconds();
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, seconds));
    }
}
