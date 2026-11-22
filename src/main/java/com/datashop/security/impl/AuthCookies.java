package com.datashop.security.impl;

import com.datashop.config.AppConfig;
import com.datashop.security.AuthCookieService;
import com.datashop.util.Constants;
import io.javalin.http.Context;
import io.javalin.http.Cookie;
import io.javalin.http.SameSite;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@RequiredArgsConstructor
public class AuthCookies implements AuthCookieService {

    private final AppConfig config;
    private final Duration ttl;

    @Override
    public void write(Context context, String token) {
        Cookie cookie = new Cookie(Constants.Security.AUTH_COOKIE, token);
        cookie.setPath(Constants.Security.COOKIE_PATH);
        cookie.setHttpOnly(true);
        cookie.setSecure(config.isSecureCookies());
        cookie.setSameSite(SameSite.LAX);
        cookie.setMaxAge(maxAgeSeconds(ttl));
        context.cookie(cookie);
    }

    @Override
    public void clear(Context context) {
        Cookie cookie = new Cookie(Constants.Security.AUTH_COOKIE, "");
        cookie.setPath(Constants.Security.COOKIE_PATH);
        cookie.setHttpOnly(true);
        cookie.setSecure(config.isSecureCookies());
        cookie.setSameSite(SameSite.LAX);
        cookie.setMaxAge(Constants.Security.COOKIE_MAX_AGE_ZERO);
        context.cookie(cookie);
    }

    private int maxAgeSeconds(Duration duration) {
        long seconds = duration.toSeconds();
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, seconds));
    }
}
