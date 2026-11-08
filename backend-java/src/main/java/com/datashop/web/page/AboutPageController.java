package com.datashop.web.page;

import com.datashop.security.CsrfTokens;
import com.datashop.security.RequestAuth;
import com.datashop.service.CartService;
import io.javalin.Javalin;

public class AboutPageController {

    private final CartService cartService;
    private final RequestAuth requestAuth;
    private final CsrfTokens csrfTokens;

    public AboutPageController(CartService cartService, RequestAuth requestAuth, CsrfTokens csrfTokens) {
        this.cartService = cartService;
        this.requestAuth = requestAuth;
        this.csrfTokens = csrfTokens;
    }

    public void register(Javalin app) {
        app.get("/about", context -> {
            PageSupport.base(context, requestAuth, cartService, csrfTokens, "about");
            PageSupport.render(context, "about");
        });
    }
}
