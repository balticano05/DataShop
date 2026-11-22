package com.datashop.web.page.order;

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
public class OrdersPageController {

    private final OrderService orderService;
    private final CartService cartService;
    private final RequestAuthenticator requestAuth;
    private final CsrfService csrfService;

    public void register(Javalin app) {
        app.get(Constants.Routes.ORDERS, context -> {
            Optional<AuthUser> user = PageSupport.requireUser(
                    context, requestAuth, cartService, csrfService, Constants.Messages.ORDERS_ACTIVE);
            if (user.isEmpty()) {
                return;
            }
            context.attribute(Constants.Attributes.ORDERS, orderService.list(user.get().getId()));
            context.attribute(
                    Constants.Attributes.CHECKOUT,
                    context.queryParam(Constants.Params.CHECKOUT) != null);
            PageSupport.render(context, Constants.Templates.ORDERS);
        });
    }
}
