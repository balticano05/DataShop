package com.datashop.web.page.auth;

import com.datashop.domain.user.User;
import com.datashop.security.AuthCookieService;
import com.datashop.security.CsrfService;
import com.datashop.security.RateLimitService;
import com.datashop.security.RequestAuthenticator;
import com.datashop.security.TokenService;
import com.datashop.service.auth.AuthService;
import com.datashop.service.cart.CartService;
import com.datashop.util.Constants;
import com.datashop.util.web.PageSupport;
import com.datashop.web.ApiException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class AuthPageController {

    private final AuthService authService;
    private final TokenService tokenService;
    private final AuthCookieService authCookies;
    private final RequestAuthenticator requestAuth;
    private final CartService cartService;
    private final CsrfService csrfService;
    private final RateLimitService rateLimiter;

    public void register(Javalin app) {
        app.get(Constants.Routes.LOGIN, context -> {
            if (PageSupport.base(
                    context, requestAuth, cartService, csrfService, Constants.Messages.LOGIN_ACTIVE).isPresent()) {
                context.redirect(Constants.Routes.HOME);
                return;
            }
            context.attribute(Constants.Attributes.NEXT, PageSupport.safeNext(context.queryParam(Constants.Params.NEXT)));
            context.attribute(Constants.Attributes.EMAIL, "");
            PageSupport.render(context, Constants.Templates.LOGIN);
        });

        app.post(Constants.Routes.LOGIN, context -> {
            csrfService.verify(context);
            rateLimiter.enforce(
                    context,
                    Constants.RateLimit.ACTION_PAGE_LOGIN,
                    Constants.RateLimit.PAGE_LOGIN,
                    Constants.RateLimit.WINDOW
            );
            String email = context.formParam(Constants.Params.EMAIL);
            String next = PageSupport.safeNext(context.formParam(Constants.Params.NEXT));
            try {
                User user = authService.login(email, context.formParam(Constants.Params.PASSWORD));
                completeLogin(context, user, next);
            } catch (ApiException exception) {
                PageSupport.base(
                        context, requestAuth, cartService, csrfService, Constants.Messages.LOGIN_ACTIVE);
                context.attribute(Constants.Attributes.NEXT, next);
                context.attribute(Constants.Attributes.EMAIL, Optional.ofNullable(email).orElse(""));
                context.attribute(Constants.Attributes.ERROR, exception.getMessage());
                context.status(exception.getStatus());
                PageSupport.render(context, Constants.Templates.LOGIN);
            }
        });

        app.get(Constants.Routes.REGISTER, context -> {
            if (PageSupport.base(
                    context, requestAuth, cartService, csrfService, Constants.Messages.LOGIN_ACTIVE).isPresent()) {
                context.redirect(Constants.Routes.HOME);
                return;
            }
            context.attribute(Constants.Attributes.NEXT, PageSupport.safeNext(context.queryParam(Constants.Params.NEXT)));
            context.attribute(Constants.Attributes.EMAIL, "");
            PageSupport.render(context, Constants.Templates.REGISTER);
        });

        app.post(Constants.Routes.REGISTER, context -> {
            csrfService.verify(context);
            rateLimiter.enforce(
                    context,
                    Constants.RateLimit.ACTION_PAGE_REGISTER,
                    Constants.RateLimit.PAGE_REGISTER,
                    Constants.RateLimit.WINDOW
            );
            String email = context.formParam(Constants.Params.EMAIL);
            String next = PageSupport.safeNext(context.formParam(Constants.Params.NEXT));
            try {
                User user = authService.register(email, context.formParam(Constants.Params.PASSWORD));
                completeLogin(context, user, next);
            } catch (ApiException exception) {
                PageSupport.base(
                        context, requestAuth, cartService, csrfService, Constants.Messages.LOGIN_ACTIVE);
                context.attribute(Constants.Attributes.NEXT, next);
                context.attribute(Constants.Attributes.EMAIL, Optional.ofNullable(email).orElse(""));
                context.attribute(Constants.Attributes.ERROR, exception.getMessage());
                context.status(exception.getStatus());
                PageSupport.render(context, Constants.Templates.REGISTER);
            }
        });

        app.post(Constants.Routes.LOGOUT, context -> {
            csrfService.verify(context);
            authCookies.clear(context);
            context.redirect(Constants.Routes.HOME);
        });
    }

    private void completeLogin(Context context, User user, String next) {
        authCookies.write(context, tokenService.issue(user));
        context.redirect(next);
    }
}
