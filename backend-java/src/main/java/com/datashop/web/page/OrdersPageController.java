package com.datashop.web.page;

import com.datashop.security.AuthUser;
import com.datashop.security.CsrfTokens;
import com.datashop.security.RequestAuth;
import com.datashop.service.CartService;
import com.datashop.service.OrderService;
import io.javalin.Javalin;

public class OrdersPageController {

    private final OrderService orderService;
    private final CartService cartService;
    private final RequestAuth requestAuth;
    private final CsrfTokens csrfTokens;

    public OrdersPageController(
            OrderService orderService,
            CartService cartService,
            RequestAuth requestAuth,
            CsrfTokens csrfTokens
    ) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.requestAuth = requestAuth;
        this.csrfTokens = csrfTokens;
    }

    public void register(Javalin app) {
        app.get("/orders", context -> {
            AuthUser user = PageSupport.requireUser(context, requestAuth, cartService, csrfTokens, "orders");
            if (user == null) {
                return;
            }
            context.attribute("orders", orderService.list(user.getId()));
            context.attribute("checkout", context.queryParam("checkout") != null);
            PageSupport.render(context, "orders");
        });
    }
}
