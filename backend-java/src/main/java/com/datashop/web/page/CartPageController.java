package com.datashop.web.page;

import com.datashop.security.AuthUser;
import com.datashop.security.CsrfTokens;
import com.datashop.security.RequestAuth;
import com.datashop.service.CartService;
import com.datashop.service.OrderService;
import io.javalin.Javalin;

public class CartPageController {

    private final CartService cartService;
    private final OrderService orderService;
    private final RequestAuth requestAuth;
    private final CsrfTokens csrfTokens;

    public CartPageController(
            CartService cartService,
            OrderService orderService,
            RequestAuth requestAuth,
            CsrfTokens csrfTokens
    ) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.requestAuth = requestAuth;
        this.csrfTokens = csrfTokens;
    }

    public void register(Javalin app) {
        app.get("/cart", context -> {
            AuthUser user = PageSupport.requireUser(context, requestAuth, cartService, csrfTokens, "cart");
            if (user == null) {
                return;
            }
            context.attribute("cart", cartService.get(user.getId()));
            PageSupport.render(context, "cart");
        });

        app.post("/cart/items", context -> {
            csrfTokens.verify(context);
            AuthUser user = requireUser(context);
            if (user == null) {
                return;
            }
            String productId = context.formParam("productId");
            int quantity = PageSupport.intParam(context, "quantity", 1, 1, 99);
            cartService.addItem(user.getId(), productId, quantity);
            String next = PageSupport.safeNext(context.formParam("next"));
            context.redirect(next.equals("/") ? "/cart" : next);
        });

        app.post("/cart/items/{id}/update", context -> {
            csrfTokens.verify(context);
            AuthUser user = requireUser(context);
            if (user == null) {
                return;
            }
            int quantity = PageSupport.intParam(context, "quantity", 1, 0, 99);
            if (quantity == 0) {
                cartService.removeItem(user.getId(), context.pathParam("id"));
            } else {
                cartService.updateItem(user.getId(), context.pathParam("id"), quantity);
            }
            context.redirect("/cart");
        });

        app.post("/cart/items/{id}/delete", context -> {
            csrfTokens.verify(context);
            AuthUser user = requireUser(context);
            if (user == null) {
                return;
            }
            cartService.removeItem(user.getId(), context.pathParam("id"));
            context.redirect("/cart");
        });

        app.post("/cart/promo", context -> {
            csrfTokens.verify(context);
            AuthUser user = requireUser(context);
            if (user == null) {
                return;
            }
            cartService.applyPromo(user.getId(), context.formParam("code"));
            context.redirect("/cart");
        });

        app.post("/cart/promo/remove", context -> {
            csrfTokens.verify(context);
            AuthUser user = requireUser(context);
            if (user == null) {
                return;
            }
            cartService.removePromo(user.getId());
            context.redirect("/cart");
        });

        app.post("/orders/checkout", context -> {
            csrfTokens.verify(context);
            AuthUser user = requireUser(context);
            if (user == null) {
                return;
            }
            orderService.checkout(user.getId());
            context.redirect("/orders?checkout=1");
        });
    }

    private AuthUser requireUser(io.javalin.http.Context context) {
        AuthUser user = requestAuth.optional(context).orElse(null);
        if (user == null) {
            PageSupport.redirectToLogin(context, "/cart");
        }
        return user;
    }
}
