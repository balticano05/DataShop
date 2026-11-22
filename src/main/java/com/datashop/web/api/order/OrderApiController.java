package com.datashop.web.api.order;

import com.datashop.domain.user.AuthUser;
import com.datashop.security.RateLimitService;
import com.datashop.security.RequestAuthenticator;
import com.datashop.service.order.OrderService;
import com.datashop.util.Constants;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderApiController {

    private final OrderService orderService;
    private final RequestAuthenticator requestAuth;
    private final RateLimitService rateLimiter;

    public void register(Javalin app) {
        app.post(Constants.Routes.API_CHECKOUT, context -> {
            AuthUser user = requestAuth.require(context);
            rateLimiter.enforce(
                    context,
                    Constants.RateLimit.ACTION_CHECKOUT,
                    Constants.RateLimit.CHECKOUT,
                    Constants.RateLimit.WINDOW
            );
            context.status(Constants.Http.Status.CREATED).json(orderService.checkout(user.getId()));
        });

        app.get(Constants.Routes.API_ORDERS, context -> {
            AuthUser user = requestAuth.require(context);
            context.json(orderService.list(user.getId()));
        });

        app.get(Constants.Routes.API_ORDER_BY_ID, context -> {
            AuthUser user = requestAuth.require(context);
            context.json(orderService.get(user.getId(), context.pathParam(Constants.Params.ID)));
        });
    }
}
