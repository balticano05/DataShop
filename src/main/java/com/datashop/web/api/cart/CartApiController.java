package com.datashop.web.api.cart;

import com.datashop.domain.user.AuthUser;
import com.datashop.security.RequestAuthenticator;
import com.datashop.service.cart.CartService;
import com.datashop.util.Constants;
import com.datashop.util.web.Errors;
import com.datashop.util.web.RequestBody;
import com.datashop.web.dto.cart.AddCartItemRequest;
import com.datashop.web.dto.cart.ApplyPromoRequest;
import com.datashop.web.dto.cart.UpdateCartItemRequest;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;
    private final RequestAuthenticator requestAuth;

    public void register(Javalin app) {
        app.get(Constants.Routes.API_CART, context -> {
            AuthUser user = requestAuth.require(context);
            context.json(cartService.get(user.getId()));
        });

        app.post(Constants.Routes.API_CART_ITEMS, context -> {
            AuthUser user = requestAuth.require(context);
            AddCartItemRequest request = RequestBody.require(context, AddCartItemRequest.class);
            context.status(Constants.Http.Status.CREATED).json(
                    cartService.addItem(user.getId(), request.productId(), request.quantity()));
        });

        app.patch(Constants.Routes.API_CART_ITEM_BY_ID, context -> {
            AuthUser user = requestAuth.require(context);
            UpdateCartItemRequest request = RequestBody.require(context, UpdateCartItemRequest.class);
            context.json(cartService.updateItem(
                    user.getId(), context.pathParam(Constants.Params.ID), request.quantity()));
        });

        app.delete(Constants.Routes.API_CART_ITEM_BY_ID, context -> {
            AuthUser user = requestAuth.require(context);
            context.json(cartService.removeItem(user.getId(), context.pathParam(Constants.Params.ID)));
        });

        app.post(Constants.Routes.API_CART_PROMO, context -> {
            AuthUser user = requestAuth.require(context);
            ApplyPromoRequest request = RequestBody.require(context, ApplyPromoRequest.class);
            if (request.code() == null || request.code().isBlank()) {
                throw Errors.badRequest(Constants.Messages.CODE_REQUIRED);
            }
            if (request.code().length() > Constants.Validation.PROMO_CODE_MAX_LENGTH) {
                throw Errors.badRequest(Constants.Messages.CODE_TOO_LONG);
            }
            context.status(Constants.Http.Status.CREATED).json(
                    cartService.applyPromo(user.getId(), request.code()));
        });

        app.delete(Constants.Routes.API_CART_PROMO, context -> {
            AuthUser user = requestAuth.require(context);
            context.json(cartService.removePromo(user.getId()));
        });
    }
}
