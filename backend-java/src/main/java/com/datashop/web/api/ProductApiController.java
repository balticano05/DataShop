package com.datashop.web.api;

import com.datashop.security.RequestAuth;
import com.datashop.service.ProductService;
import com.datashop.web.dto.CreateProductRequest;
import com.datashop.web.dto.ProductView;
import com.datashop.web.dto.UpdateProductRequest;
import io.javalin.Javalin;

public class ProductApiController {

    private final ProductService productService;
    private final RequestAuth requestAuth;

    public ProductApiController(ProductService productService, RequestAuth requestAuth) {
        this.productService = productService;
        this.requestAuth = requestAuth;
    }

    public void register(Javalin app) {
        app.get("/api/products", context -> {
            ProductService.CatalogResult result = productService.list(ProductQueryParser.parse(context));
            context.header("X-Cache", result.cacheHit() ? "HIT" : "MISS");
            context.json(result.response());
        });

        app.get("/api/products/{id}", context ->
                context.json(productService.get(context.pathParam("id"))));

        app.post("/api/products", context -> {
            requestAuth.requireAdmin(context);
            CreateProductRequest request = AuthApiController.body(context, CreateProductRequest.class);
            ProductView product = productService.create(request);
            context.status(201).json(product);
        });

        app.patch("/api/products/{id}", context -> {
            requestAuth.requireAdmin(context);
            UpdateProductRequest request = AuthApiController.body(context, UpdateProductRequest.class);
            context.json(productService.update(context.pathParam("id"), request));
        });

        app.delete("/api/products/{id}", context -> {
            requestAuth.requireAdmin(context);
            productService.delete(context.pathParam("id"));
            context.status(204);
        });
    }
}
