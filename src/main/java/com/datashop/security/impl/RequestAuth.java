package com.datashop.security.impl;

import com.datashop.domain.user.AuthUser;
import com.datashop.security.RequestAuthenticator;
import com.datashop.security.TokenService;
import com.datashop.util.Constants;
import com.datashop.util.web.Errors;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class RequestAuth implements RequestAuthenticator {

    private final TokenService tokenService;

    @Override
    public Optional<AuthUser> optional(Context context) {
        Object cached = context.attribute(Constants.Security.AUTH_ATTRIBUTE);
        if (cached instanceof AuthUser authUser) {
            return Optional.of(authUser);
        }
        Optional<AuthUser> authUser = extract(context).flatMap(tokenService::parse);
        authUser.ifPresent(user -> context.attribute(Constants.Security.AUTH_ATTRIBUTE, user));
        return authUser;
    }

    @Override
    public AuthUser require(Context context) {
        return optional(context).orElseThrow(
                () -> Errors.unauthorized(Constants.Messages.AUTHENTICATION_REQUIRED));
    }

    @Override
    public AuthUser requireAdmin(Context context) {
        AuthUser user = require(context);
        if (!user.isAdmin()) {
            throw Errors.forbidden(Constants.Messages.ADMIN_ACCESS_REQUIRED);
        }
        return user;
    }

    private Optional<String> extract(Context context) {
        String authorization = context.header(Constants.Http.AUTHORIZATION_HEADER);
        if (authorization != null && authorization.startsWith(Constants.Http.BEARER_PREFIX)) {
            return Optional.of(authorization.substring(Constants.Http.BEARER_PREFIX.length()).trim());
        }
        if (context.path().startsWith(Constants.Http.API_PREFIX)) {
            return Optional.empty();
        }
        return Optional.ofNullable(context.cookie(Constants.Security.AUTH_COOKIE))
                .filter(cookie -> !cookie.isBlank());
    }
}
