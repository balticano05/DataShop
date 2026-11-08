package com.datashop.web.api;

import com.datashop.security.AuthUser;
import com.datashop.security.RequestAuth;
import com.datashop.service.CartService;
import com.datashop.web.Errors;
import com.datashop.web.dto.AddCartItemRequest;
import com.datashop.web.dto.ApplyPromoRequest;
import com.datashop.web.dto.UpdateCartItemRequest;
import io.javalin.Javalin;

public class CartApiController {

    private final CartService cartService;
    private final RequestAuth requestAuth;

    public CartApiController(CartService cartService, RequestAuth requestAuth) {
        this.cartService = cartService;
        this.requestAuth = requestAuth;
    }

    public void register(Javalin app) {
        app.get("/api/cart", context -> {
            AuthUser user = requestAuth.require(context);
            context.json(cartService.get(user.getId()));
        });

        app.post("/api/cart/items", context -> {
            AuthUser user = requestAuth.require(context);
            AddCartItemRequest request = AuthApiController.body(context, AddCartItemRequest.class);
            context.status(201).json(cartService.addItem(user.getId(), request.productId(), request.quantity()));
        });

        app.patch("/api/cart/items/{id}", context -> {
            AuthUser user = requestAuth.require(context);
            UpdateCartItemRequest request = AuthApiController.body(context, UpdateCartItemRequest.class);
            context.json(cartService.updateItem(user.getId(), context.pathParam("id"), request.quantity()));
        });

        app.delete("/api/cart/items/{id}", context -> {
            AuthUser user = requestAuth.require(context);
            context.json(cartService.removeItem(user.getId(), context.pathParam("id")));
        });

        app.post("/api/cart/apply-promo", context -> {
            AuthUser user = requestAuth.require(context);
            ApplyPromoRequest request = AuthApiController.body(context, ApplyPromoRequest.class);
            if (request.code() == null || request.code().isBlank()) {
                throw Errors.badRequest("code is required");
            }
            if (request.code().length() > 64) {
                throw Errors.badRequest("code must be at most 64 characters");
            }
            context.status(201).json(cartService.applyPromo(user.getId(), request.code()));
        });

        app.delete("/api/cart/apply-promo", context -> {
            AuthUser user = requestAuth.require(context);
            context.json(cartService.removePromo(user.getId()));
        });
    }
}
