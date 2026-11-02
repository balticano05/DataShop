package com.datashop.web.page;

import com.datashop.query.ProductQuery;
import com.datashop.security.AuthUser;
import com.datashop.security.CsrfTokens;
import com.datashop.security.RequestAuth;
import com.datashop.service.CartService;
import com.datashop.service.DownloadService;
import com.datashop.service.OrderService;
import com.datashop.service.ProductService;
import com.datashop.web.Errors;
import io.javalin.Javalin;

import java.util.UUID;

public class ProductPageController {

    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    private final DownloadService downloadService;
    private final RequestAuth requestAuth;
    private final CsrfTokens csrfTokens;

    public ProductPageController(
            ProductService productService,
            CartService cartService,
            OrderService orderService,
            DownloadService downloadService,
            RequestAuth requestAuth,
            CsrfTokens csrfTokens
    ) {
        this.productService = productService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.downloadService = downloadService;
        this.requestAuth = requestAuth;
        this.csrfTokens = csrfTokens;
    }

    public void register(Javalin app) {
        app.get("/products/{id}", context -> {
            AuthUser user = PageSupport.base(context, requestAuth, cartService, csrfTokens, "catalog");
            var product = productService.get(context.pathParam("id"));
            var related = productService.list(ProductQuery.builder().limit(6).build())
                    .response().getItems().stream()
                    .filter(item -> !item.getId().equals(product.getId()))
                    .limit(6)
                    .toList();

            boolean owned = user != null && orderService.hasPurchased(user.getId(), UUID.fromString(product.getId()));
            int inCart = 0;
            if (user != null) {
                inCart = cartService.get(user.getId()).getItems().stream()
                        .filter(item -> item.getProductId().equals(product.getId()))
                        .mapToInt(item -> item.getQuantity())
                        .findFirst()
                        .orElse(0);
            }

            context.attribute("product", product);
            context.attribute("related", related);
            context.attribute("owned", owned);
            context.attribute("inCart", inCart);
            PageSupport.render(context, "product");
        });

        app.post("/downloads/{productId}", context -> {
            csrfTokens.verify(context);
            AuthUser user = requestAuth.optional(context).orElse(null);
            if (user == null) {
                PageSupport.redirectToLogin(context, "/orders");
                return;
            }
            var link = downloadService.issue(user.getId(), context.pathParam("productId"));
            context.redirect(link.getUrl());
        });
    }
}
