package com.datashop.service.auth.impl;

import com.datashop.security.AuthCookieService;
import com.datashop.service.auth.AuthSessionService;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthSessionServiceImpl implements AuthSessionService {

    private final AuthCookieService authCookies;

    @Override
    public void start(Context context, String token) {
        authCookies.write(context, token);
    }

    @Override
    public void logout(Context context) {
        authCookies.clear(context);
    }
}
