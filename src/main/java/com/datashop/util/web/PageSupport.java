package com.datashop.util.web;

import com.datashop.domain.user.AuthUser;
import com.datashop.security.CsrfService;
import com.datashop.security.RequestAuthenticator;
import com.datashop.service.cart.CartService;
import com.datashop.util.Constants;
import io.javalin.http.Context;
import lombok.experimental.UtilityClass;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Optional;

@UtilityClass
public final class PageSupport {

    public static Optional<AuthUser> base(
            Context context,
            RequestAuthenticator requestAuth,
            CartService cartService,
            CsrfService csrfService,
            String active
    ) {
        Optional<AuthUser> user = requestAuth.optional(context);
        context.attribute(Constants.Attributes.USER, user.orElse(null));
        context.attribute(Constants.Attributes.ACTIVE, active);
        context.attribute(
                Constants.Attributes.CART_COUNT,
                user.map(value -> cartService.get(value.getId()).getItemCount()).orElse(0));
        context.attribute(Constants.Attributes.CSRF_TOKEN, csrfService.token(context));
        return user;
    }

    public static Optional<AuthUser> requireUser(
            Context context,
            RequestAuthenticator requestAuth,
            CartService cartService,
            CsrfService csrfService,
            String active
    ) {
        Optional<AuthUser> user = base(context, requestAuth, cartService, csrfService, active);
        if (user.isEmpty()) {
            redirectToLogin(context, context.path());
        }
        return user;
    }

    public static Optional<AuthUser> requireAdmin(
            Context context,
            RequestAuthenticator requestAuth,
            CartService cartService,
            CsrfService csrfService,
            String active
    ) {
        Optional<AuthUser> user = requireUser(context, requestAuth, cartService, csrfService, active);
        user.filter(value -> !value.isAdmin()).ifPresent(value -> {
            throw Errors.forbidden(Constants.Messages.ADMIN_ACCESS_REQUIRED);
        });
        return user;
    }

    public static void render(Context context, String template) {
        context.render(template, new LinkedHashMap<>(context.attributeMap()));
    }

    public static void redirectToLogin(Context context, String next) {
        context.redirect(Constants.Redirects.LOGIN_NEXT_PREFIX
                + URLEncoder.encode(safeNext(next), StandardCharsets.UTF_8));
    }

    public static String safeNext(String next) {
        return Optional.ofNullable(next)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .filter(value -> value.startsWith(Constants.Routes.HOME))
                .filter(value -> !value.startsWith(Constants.Routes.HOME + Constants.Routes.HOME))
                .filter(value -> !value.contains("\\"))
                .filter(value -> !value.contains("\r"))
                .filter(value -> !value.contains("\n"))
                .orElse(Constants.Routes.HOME);
    }

    public static int intParam(Context context, String name, int fallback, int min, int max) {
        return Optional.ofNullable(context.formParam(name))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> parseInteger(name, value, min, max))
                .orElse(fallback);
    }

    private static int parseInteger(String name, String raw, int min, int max) {
        try {
            int value = Integer.parseInt(raw);
            if (value < min || value > max) {
                throw Errors.badRequest(
                        Constants.Messages.FIELD_BETWEEN.formatted(name, min, max));
            }
            return value;
        } catch (NumberFormatException exception) {
            throw Errors.badRequest(Constants.Messages.FIELD_INTEGER.formatted(name));
        }
    }
}
