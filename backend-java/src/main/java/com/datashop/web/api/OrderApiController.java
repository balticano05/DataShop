package com.datashop.web.api;

import com.datashop.security.AuthUser;
import com.datashop.security.RateLimiter;
import com.datashop.security.RequestAuth;
import com.datashop.service.OrderService;
import io.javalin.Javalin;

import java.time.Duration;

public class OrderApiController {

    private final OrderService orderService;
    private final RequestAuth requestAuth;
    private final RateLimiter rateLimiter;

    public OrderApiController(OrderService orderService, RequestAuth requestAuth, RateLimiter rateLimiter) {
        this.orderService = orderService;
        this.requestAuth = requestAuth;
        this.rateLimiter = rateLimiter;
    }

    public void register(Javalin app) {
        app.post("/api/orders/checkout", context -> {
            AuthUser user = requestAuth.require(context);
            rateLimiter.enforce(context, "checkout", 10, Duration.ofMinutes(1));
            context.status(201).json(orderService.checkout(user.getId()));
        });

        app.get("/api/orders", context -> {
            AuthUser user = requestAuth.require(context);
            context.json(orderService.list(user.getId()));
        });

        app.get("/api/orders/{id}", context -> {
            AuthUser user = requestAuth.require(context);
            context.json(orderService.get(user.getId(), context.pathParam("id")));
        });
    }
}
