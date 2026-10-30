package com.datashop.web.page;

import com.datashop.security.CsrfTokens;
import com.datashop.security.RequestAuth;
import com.datashop.service.CartService;
import com.datashop.service.ProductService;
import com.datashop.web.api.ProductQueryParser;
import io.javalin.Javalin;

public class CatalogPageController {

    private final ProductService productService;
    private final CartService cartService;
    private final RequestAuth requestAuth;
    private final CsrfTokens csrfTokens;

    public CatalogPageController(
            ProductService productService,
            CartService cartService,
            RequestAuth requestAuth,
            CsrfTokens csrfTokens
    ) {
        this.productService = productService;
        this.cartService = cartService;
        this.requestAuth = requestAuth;
        this.csrfTokens = csrfTokens;
    }

    public void register(Javalin app) {
        app.get("/", context -> {
            PageSupport.base(context, requestAuth, cartService, csrfTokens, "catalog");
            var query = ProductQueryParser.parse(context);
            var result = productService.list(query);
            context.attribute("products", result.response().getItems());
            context.attribute("meta", result.response().getMeta());
            context.attribute("query", query);
            PageSupport.render(context, "catalog");
        });
    }
}
