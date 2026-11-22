package com.datashop.web.api.auth;

import com.datashop.domain.user.AuthUser;
import com.datashop.domain.user.User;
import com.datashop.security.RateLimitService;
import com.datashop.security.RequestAuthenticator;
import com.datashop.security.TokenService;
import com.datashop.service.auth.AuthService;
import com.datashop.service.auth.AuthSessionService;
import com.datashop.util.Constants;
import com.datashop.util.web.RequestBody;
import com.datashop.web.dto.auth.AuthResponse;
import com.datashop.web.dto.auth.LoginRequest;
import com.datashop.web.dto.auth.RegisterRequest;
import com.datashop.web.dto.auth.UserView;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class AuthApiController {

    private final AuthService authService;
    private final AuthSessionService authSessionService;
    private final TokenService tokenService;
    private final RequestAuthenticator requestAuth;
    private final RateLimitService rateLimiter;

    public void register(Javalin app) {
        app.post(Constants.Routes.API_AUTH_REGISTER, registerHandler());
        app.post(Constants.Routes.API_AUTH_LOGIN, loginHandler());
        app.get(Constants.Routes.API_AUTH_ME, meHandler());
        app.get(Constants.Routes.API_AUTH_ADMIN_CHECK, adminCheckHandler());
        app.post(Constants.Routes.API_AUTH_LOGOUT, logoutHandler());
    }

    private Handler registerHandler() {
        return context -> {
            rateLimiter.enforce(
                    context,
                    Constants.RateLimit.ACTION_REGISTER,
                    Constants.RateLimit.API_REGISTER,
                    Constants.RateLimit.WINDOW
            );
            RegisterRequest request = RequestBody.require(context, RegisterRequest.class);
            User user = authService.register(request.email(), request.password());
            authenticate(context, user, Constants.Http.Status.CREATED);
        };
    }

    private Handler loginHandler() {
        return context -> {
            rateLimiter.enforce(
                    context,
                    Constants.RateLimit.ACTION_LOGIN,
                    Constants.RateLimit.API_LOGIN,
                    Constants.RateLimit.WINDOW
            );
            LoginRequest request = RequestBody.require(context, LoginRequest.class);
            User user = authService.login(request.email(), request.password());
            authenticate(context, user, Constants.Http.Status.OK);
        };
    }

    private Handler meHandler() {
        return context -> {
            AuthUser authUser = requestAuth.require(context);
            User user = authService.require(authUser.getId());
            context.json(UserView.from(user));
        };
    }

    private Handler adminCheckHandler() {
        return context -> {
            AuthUser authUser = requestAuth.require(context);
            User user = authService.requireAdmin(authUser.getId());
            context.json(Map.of(
                    Constants.Json.MESSAGE, Constants.Messages.ADMIN_ACCESS_GRANTED,
                    Constants.Json.USER, UserView.from(user)
            ));
        };
    }

    private Handler logoutHandler() {
        return context -> {
            authSessionService.logout(context);
            context.status(Constants.Http.Status.NO_CONTENT);
        };
    }

    private void authenticate(Context context, User user, int status) {
        String token = tokenService.issue(user);
        authSessionService.start(context, token);
        context.status(status).json(AuthResponse.builder()
                .accessToken(token)
                .user(UserView.from(user))
                .build());
    }
}
