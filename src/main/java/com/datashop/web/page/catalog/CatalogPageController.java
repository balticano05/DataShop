package com.datashop.web.page.catalog;

import com.datashop.security.CsrfService;
import com.datashop.security.RequestAuthenticator;
import com.datashop.service.cart.CartService;
import com.datashop.service.catalog.ProductService;
import com.datashop.util.Constants;
import com.datashop.util.web.PageSupport;
import com.datashop.util.web.ProductQueryParser;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CatalogPageController {

    private final ProductService productService;
    private final CartService cartService;
    private final RequestAuthenticator requestAuth;
    private final CsrfService csrfService;

    public void register(Javalin app) {
        app.get(Constants.Routes.HOME, context -> {
            PageSupport.base(
                    context, requestAuth, cartService, csrfService, Constants.Messages.CATALOG_ACTIVE);
            var query = ProductQueryParser.parse(context);
            var result = productService.list(query);
            context.attribute(Constants.Attributes.PRODUCTS, result.response().getItems());
            context.attribute(Constants.Attributes.META, result.response().getMeta());
            context.attribute(Constants.Attributes.QUERY, query);
            PageSupport.render(context, Constants.Templates.CATALOG);
        });
    }
}
