package com.datashop.web.page;

import com.datashop.domain.User;
import com.datashop.security.AuthCookies;
import com.datashop.security.CsrfTokens;
import com.datashop.security.JwtTokenService;
import com.datashop.security.RateLimiter;
import com.datashop.security.RequestAuth;
import com.datashop.service.AuthService;
import com.datashop.service.CartService;
import com.datashop.web.ApiException;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.time.Duration;

public class AuthPageController {

    private final AuthService authService;
    private final JwtTokenService jwtTokenService;
    private final AuthCookies authCookies;
    private final RequestAuth requestAuth;
    private final CartService cartService;
    private final CsrfTokens csrfTokens;
    private final RateLimiter rateLimiter;

    public AuthPageController(
            AuthService authService,
            JwtTokenService jwtTokenService,
            AuthCookies authCookies,
            RequestAuth requestAuth,
            CartService cartService,
            CsrfTokens csrfTokens,
            RateLimiter rateLimiter
    ) {
        this.authService = authService;
        this.jwtTokenService = jwtTokenService;
        this.authCookies = authCookies;
        this.requestAuth = requestAuth;
        this.cartService = cartService;
        this.csrfTokens = csrfTokens;
        this.rateLimiter = rateLimiter;
    }

    public void register(Javalin app) {
        app.get("/login", context -> {
            if (PageSupport.base(context, requestAuth, cartService, csrfTokens, "login") != null) {
                context.redirect("/");
                return;
            }
            context.attribute("next", PageSupport.safeNext(context.queryParam("next")));
            context.attribute("email", "");
            PageSupport.render(context, "login");
        });

        app.post("/login", context -> {
            csrfTokens.verify(context);
            rateLimiter.enforce(context, "page-login", 20, Duration.ofMinutes(1));
            String email = context.formParam("email");
            String next = PageSupport.safeNext(context.formParam("next"));
            try {
                User user = authService.login(email, context.formParam("password"));
                completeLogin(context, user, next);
            } catch (ApiException exception) {
                PageSupport.base(context, requestAuth, cartService, csrfTokens, "login");
                context.attribute("next", next);
                context.attribute("email", email == null ? "" : email);
                context.attribute("error", exception.getMessage());
                context.status(exception.getStatus());
                PageSupport.render(context, "login");
            }
        });

        app.get("/register", context -> {
            if (PageSupport.base(context, requestAuth, cartService, csrfTokens, "login") != null) {
                context.redirect("/");
                return;
            }
            context.attribute("next", PageSupport.safeNext(context.queryParam("next")));
            context.attribute("email", "");
            PageSupport.render(context, "register");
        });

        app.post("/register", context -> {
            csrfTokens.verify(context);
            rateLimiter.enforce(context, "page-register", 10, Duration.ofMinutes(1));
            String email = context.formParam("email");
            String next = PageSupport.safeNext(context.formParam("next"));
            try {
                User user = authService.register(email, context.formParam("password"));
                completeLogin(context, user, next);
            } catch (ApiException exception) {
                PageSupport.base(context, requestAuth, cartService, csrfTokens, "login");
                context.attribute("next", next);
                context.attribute("email", email == null ? "" : email);
                context.attribute("error", exception.getMessage());
                context.status(exception.getStatus());
                PageSupport.render(context, "register");
            }
        });

        app.post("/logout", context -> {
            csrfTokens.verify(context);
            authCookies.clear(context);
            context.redirect("/");
        });
    }

    private void completeLogin(Context context, User user, String next) {
        authCookies.write(context, jwtTokenService.issue(user));
        context.redirect(next);
    }
}
