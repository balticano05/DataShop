package com.datashop.web.page;

import com.datashop.security.AuthUser;
import com.datashop.security.CsrfTokens;
import com.datashop.security.RequestAuth;
import com.datashop.service.CartService;
import com.datashop.web.Errors;
import io.javalin.http.Context;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class PageSupport {

    private PageSupport() {
    }

    public static AuthUser base(
            Context context,
            RequestAuth requestAuth,
            CartService cartService,
            CsrfTokens csrfTokens,
            String active
    ) {
        AuthUser user = requestAuth.optional(context).orElse(null);
        context.attribute("user", user);
        context.attribute("active", active);
        context.attribute("cartCount", user == null ? 0 : cartService.get(user.getId()).getItemCount());
        context.attribute("csrfToken", csrfTokens.token(context));
        return user;
    }

    public static AuthUser requireUser(
            Context context,
            RequestAuth requestAuth,
            CartService cartService,
            CsrfTokens csrfTokens,
            String active
    ) {
        AuthUser user = base(context, requestAuth, cartService, csrfTokens, active);
        if (user == null) {
            redirectToLogin(context, context.path());
            return null;
        }
        return user;
    }

    public static AuthUser requireAdmin(
            Context context,
            RequestAuth requestAuth,
            CartService cartService,
            CsrfTokens csrfTokens,
            String active
    ) {
        AuthUser user = requireUser(context, requestAuth, cartService, csrfTokens, active);
        if (user != null && !user.isAdmin()) {
            throw Errors.forbidden("Admin access required");
        }
        return user;
    }

    public static void render(Context context, String template) {
        context.render(template, new java.util.LinkedHashMap<>(context.attributeMap()));
    }

    public static void redirectToLogin(Context context, String next) {
        context.redirect("/login?next=" + URLEncoder.encode(safeNext(next), StandardCharsets.UTF_8));
    }

    public static String safeNext(String next) {
        if (next == null || next.isBlank()) {
            return "/";
        }
        String value = next.trim();
        if (!value.startsWith("/") || value.startsWith("//") || value.contains("\\")
                || value.contains("\r") || value.contains("\n")) {
            return "/";
        }
        return value;
    }

    public static int intParam(Context context, String name, int fallback, int min, int max) {
        String raw = context.formParam(name);
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            if (value < min || value > max) {
                throw Errors.badRequest(name + " must be between " + min + " and " + max);
            }
            return value;
        } catch (NumberFormatException exception) {
            throw Errors.badRequest(name + " must be an integer");
        }
    }
}
