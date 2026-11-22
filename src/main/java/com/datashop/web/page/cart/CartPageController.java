package com.datashop.web.page.cart;

import com.datashop.domain.user.AuthUser;
import com.datashop.security.CsrfService;
import com.datashop.security.RequestAuthenticator;
import com.datashop.service.cart.CartService;
import com.datashop.service.order.OrderService;
import com.datashop.util.Constants;
import com.datashop.util.web.PageSupport;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class CartPageController {

    private final CartService cartService;
    private final OrderService orderService;
    private final RequestAuthenticator requestAuth;
    private final CsrfService csrfService;

    public void register(Javalin app) {
        app.get(Constants.Routes.CART, context -> {
            Optional<AuthUser> user = PageSupport.requireUser(
                    context, requestAuth, cartService, csrfService, Constants.Messages.CART_ACTIVE);
            if (user.isEmpty()) {
                return;
            }
            context.attribute(Constants.Attributes.CART, cartService.get(user.get().getId()));
            PageSupport.render(context, Constants.Templates.CART);
        });

        app.post(Constants.Routes.CART_ITEMS, context -> {
            csrfService.verify(context);
            Optional<AuthUser> user = requireUser(context);
            if (user.isEmpty()) {
                return;
            }
            String productId = context.formParam(Constants.Params.PRODUCT_ID);
            int quantity = PageSupport.intParam(
                    context,
                    Constants.Params.QUANTITY,
                    1,
                    Constants.Validation.MIN_CART_QUANTITY,
                    Constants.Validation.MAX_CART_QUANTITY);
            cartService.addItem(user.orElseThrow().getId(), productId, quantity);
            String next = PageSupport.safeNext(context.formParam(Constants.Params.NEXT));
            context.redirect(next.equals(Constants.Routes.HOME) ? Constants.Routes.CART : next);
        });

        app.post(Constants.Routes.CART_ITEM_UPDATE, context -> {
            csrfService.verify(context);
            Optional<AuthUser> user = requireUser(context);
            if (user.isEmpty()) {
                return;
            }
            int quantity = PageSupport.intParam(
                    context,
                    Constants.Params.QUANTITY,
                    1,
                    0,
                    Constants.Validation.MAX_CART_QUANTITY);
            if (quantity == 0) {
                cartService.removeItem(user.orElseThrow().getId(), context.pathParam(Constants.Params.ID));
            } else {
                cartService.updateItem(user.orElseThrow().getId(), context.pathParam(Constants.Params.ID), quantity);
            }
            context.redirect(Constants.Routes.CART);
        });

        app.post(Constants.Routes.CART_ITEM_DELETE, context -> {
            csrfService.verify(context);
            Optional<AuthUser> user = requireUser(context);
            if (user.isEmpty()) {
                return;
            }
            cartService.removeItem(user.orElseThrow().getId(), context.pathParam(Constants.Params.ID));
            context.redirect(Constants.Routes.CART);
        });

        app.post(Constants.Routes.CART_PROMO, context -> {
            csrfService.verify(context);
            Optional<AuthUser> user = requireUser(context);
            if (user.isEmpty()) {
                return;
            }
            cartService.applyPromo(user.orElseThrow().getId(), context.formParam(Constants.Params.CODE));
            context.redirect(Constants.Routes.CART);
        });

        app.post(Constants.Routes.CART_PROMO_REMOVE, context -> {
            csrfService.verify(context);
            Optional<AuthUser> user = requireUser(context);
            if (user.isEmpty()) {
                return;
            }
            cartService.removePromo(user.orElseThrow().getId());
            context.redirect(Constants.Routes.CART);
        });

        app.post(Constants.Routes.CHECKOUT, context -> {
            csrfService.verify(context);
            Optional<AuthUser> user = requireUser(context);
            if (user.isEmpty()) {
                return;
            }
            orderService.checkout(user.orElseThrow().getId());
            context.redirect(Constants.Redirects.ORDERS_CHECKOUT);
        });
    }

    private Optional<AuthUser> requireUser(io.javalin.http.Context context) {
        Optional<AuthUser> user = requestAuth.optional(context);
        if (user.isEmpty()) {
            PageSupport.redirectToLogin(context, Constants.Routes.CART);
        }
        return user;
    }
}
