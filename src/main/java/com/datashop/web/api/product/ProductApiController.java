package com.datashop.web.api.product;

import com.datashop.security.RequestAuthenticator;
import com.datashop.service.catalog.ProductService;
import com.datashop.util.Constants;
import com.datashop.util.web.ProductQueryParser;
import com.datashop.util.web.RequestBody;
import com.datashop.web.dto.product.CreateProductRequest;
import com.datashop.web.dto.product.ProductView;
import com.datashop.web.dto.product.UpdateProductRequest;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductApiController {

    private final ProductService productService;
    private final RequestAuthenticator requestAuth;

    public void register(Javalin app) {
        app.get(Constants.Routes.API_PRODUCTS, context -> {
            ProductService.CatalogResult result = productService.list(ProductQueryParser.parse(context));
            context.header(
                    Constants.Http.CACHE_HEADER,
                    result.cacheHit() ? Constants.Http.CACHE_HIT : Constants.Http.CACHE_MISS
            );
            context.json(result.response());
        });

        app.get(Constants.Routes.API_PRODUCT_BY_ID, context ->
                context.json(productService.get(context.pathParam(Constants.Params.ID))));

        app.post(Constants.Routes.API_PRODUCTS, context -> {
            requestAuth.requireAdmin(context);
            CreateProductRequest request = RequestBody.require(context, CreateProductRequest.class);
            ProductView product = productService.create(request);
            context.status(Constants.Http.Status.CREATED).json(product);
        });

        app.patch(Constants.Routes.API_PRODUCT_BY_ID, context -> {
            requestAuth.requireAdmin(context);
            UpdateProductRequest request = RequestBody.require(context, UpdateProductRequest.class);
            context.json(productService.update(context.pathParam(Constants.Params.ID), request));
        });

        app.delete(Constants.Routes.API_PRODUCT_BY_ID, context -> {
            requestAuth.requireAdmin(context);
            productService.delete(context.pathParam(Constants.Params.ID));
            context.status(Constants.Http.Status.NO_CONTENT);
        });
    }
}
