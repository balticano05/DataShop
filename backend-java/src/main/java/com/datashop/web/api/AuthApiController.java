package com.datashop.web.api;

import com.datashop.domain.User;
import com.datashop.domain.UserRole;
import com.datashop.security.AuthCookies;
import com.datashop.security.AuthUser;
import com.datashop.security.JwtTokenService;
import com.datashop.security.RateLimiter;
import com.datashop.security.RequestAuth;
import com.datashop.service.AuthService;
import com.datashop.web.Errors;
import com.datashop.web.dto.AuthResponse;
import com.datashop.web.dto.LoginRequest;
import com.datashop.web.dto.RegisterRequest;
import com.datashop.web.dto.UserView;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.time.Duration;
import java.util.Map;

public class AuthApiController {

    private final AuthService authService;
    private final JwtTokenService jwtTokenService;
    private final AuthCookies authCookies;
    private final RequestAuth requestAuth;
    private final RateLimiter rateLimiter;

    public AuthApiController(
            AuthService authService,
            JwtTokenService jwtTokenService,
            AuthCookies authCookies,
            RequestAuth requestAuth,
            RateLimiter rateLimiter
    ) {
        this.authService = authService;
        this.jwtTokenService = jwtTokenService;
        this.authCookies = authCookies;
        this.requestAuth = requestAuth;
        this.rateLimiter = rateLimiter;
    }

    public void register(Javalin app) {
        app.post("/api/auth/register", registerHandler());
        app.post("/api/auth/login", loginHandler());
        app.get("/api/auth/me", meHandler());
        app.get("/api/auth/admin-check", adminCheckHandler());
        app.post("/api/auth/logout", logoutHandler());
    }

    private Handler registerHandler() {
        return context -> {
            rateLimiter.enforce(context, "register", 5, Duration.ofMinutes(1));
            RegisterRequest request = body(context, RegisterRequest.class);
            User user = authService.register(request.email(), request.password());
            authenticate(context, user, 201);
        };
    }

    private Handler loginHandler() {
        return context -> {
            rateLimiter.enforce(context, "login", 5, Duration.ofMinutes(1));
            LoginRequest request = body(context, LoginRequest.class);
            User user = authService.login(request.email(), request.password());
            authenticate(context, user, 200);
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
            User user = authService.require(authUser.getId());
            if (user.getRole() != UserRole.ADMIN) {
                throw Errors.forbidden("Insufficient permissions");
            }
            context.json(Map.of(
                    "message", "admin access granted",
                    "user", UserView.from(user)
            ));
        };
    }

    private Handler logoutHandler() {
        return context -> {
            authCookies.clear(context);
            context.status(204);
        };
    }

    private void authenticate(Context context, User user, int status) {
        String token = jwtTokenService.issue(user);
        authCookies.write(context, token);
        context.status(status).json(AuthResponse.builder()
                .accessToken(token)
                .user(UserView.from(user))
                .build());
    }

    static <T> T body(Context context, Class<T> type) {
        try {
            T body = context.bodyAsClass(type);
            if (body == null) {
                throw Errors.badRequest("Request body is required");
            }
            return body;
        } catch (io.javalin.http.BadRequestResponse exception) {
            throw Errors.badRequest("Malformed request body");
        }
    }
}
