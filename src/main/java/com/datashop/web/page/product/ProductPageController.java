package com.datashop.web.page.product;

import com.datashop.domain.product.ProductQuery;
import com.datashop.domain.user.AuthUser;
import com.datashop.security.CsrfService;
import com.datashop.security.RequestAuthenticator;
import com.datashop.service.cart.CartService;
import com.datashop.service.catalog.ProductService;
import com.datashop.service.download.DownloadService;
import com.datashop.service.order.OrderService;
import com.datashop.util.Constants;
import com.datashop.util.web.PageSupport;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class ProductPageController {

    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    private final DownloadService downloadService;
    private final RequestAuthenticator requestAuth;
    private final CsrfService csrfService;

    public void register(Javalin app) {
        app.get(Constants.Routes.PRODUCT_BY_ID, context -> {
            Optional<AuthUser> user = PageSupport.base(
                    context, requestAuth, cartService, csrfService, Constants.Messages.CATALOG_ACTIVE);
            var product = productService.get(context.pathParam(Constants.Params.ID));
            var related = productService.list(ProductQuery.builder()
                            .limit(Constants.Validation.RELATED_PRODUCTS_LIMIT)
                            .build())
                    .response().getItems().stream()
                    .filter(item -> !item.getId().equals(product.getId()))
                    .limit(Constants.Validation.RELATED_PRODUCTS_LIMIT)
                    .toList();

            boolean owned = user.map(value -> orderService.hasPurchased(
                            value.getId(), UUID.fromString(product.getId())))
                    .orElse(false);
            int inCart = user.map(value -> cartService.get(value.getId()).getItems().stream()
                            .filter(item -> item.getProductId().equals(product.getId()))
                            .mapToInt(item -> item.getQuantity())
                            .findFirst()
                            .orElse(0))
                    .orElse(0);

            context.attribute(Constants.Attributes.PRODUCT, product);
            context.attribute(Constants.Attributes.RELATED, related);
            context.attribute(Constants.Attributes.OWNED, owned);
            context.attribute(Constants.Attributes.IN_CART, inCart);
            PageSupport.render(context, Constants.Templates.PRODUCT);
        });

        app.post(Constants.Routes.DOWNLOAD_BY_PRODUCT_ID, context -> {
            csrfService.verify(context);
            requestAuth.optional(context).ifPresentOrElse(
                    user -> context.redirect(downloadService.issue(
                            user.getId(), context.pathParam(Constants.Params.PRODUCT_ID)).getUrl()),
                    () -> PageSupport.redirectToLogin(context, Constants.Routes.ORDERS));
        });
    }
}
