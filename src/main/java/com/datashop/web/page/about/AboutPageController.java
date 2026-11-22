package com.datashop.web.page.about;

import com.datashop.security.CsrfService;
import com.datashop.security.RequestAuthenticator;
import com.datashop.service.cart.CartService;
import com.datashop.util.Constants;
import com.datashop.util.web.PageSupport;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AboutPageController {

    private final CartService cartService;
    private final RequestAuthenticator requestAuth;
    private final CsrfService csrfService;

    public void register(Javalin app) {
        app.get(Constants.Routes.ABOUT, context -> {
            PageSupport.base(
                    context, requestAuth, cartService, csrfService, Constants.Messages.ABOUT_ACTIVE);
            PageSupport.render(context, Constants.Templates.ABOUT);
        });
    }
}
