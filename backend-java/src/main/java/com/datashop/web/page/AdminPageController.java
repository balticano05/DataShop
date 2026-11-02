package com.datashop.web.page;

import com.datashop.query.ProductQuery;
import com.datashop.security.AuthUser;
import com.datashop.security.CsrfTokens;
import com.datashop.security.RequestAuth;
import com.datashop.service.CartService;
import com.datashop.service.MediaService;
import com.datashop.service.ProductService;
import com.datashop.web.ApiException;
import com.datashop.web.dto.CreateProductRequest;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class AdminPageController {

    private final ProductService productService;
    private final MediaService mediaService;
    private final CartService cartService;
    private final RequestAuth requestAuth;
    private final CsrfTokens csrfTokens;

    public AdminPageController(
            ProductService productService,
            MediaService mediaService,
            CartService cartService,
            RequestAuth requestAuth,
            CsrfTokens csrfTokens
    ) {
        this.productService = productService;
        this.mediaService = mediaService;
        this.cartService = cartService;
        this.requestAuth = requestAuth;
        this.csrfTokens = csrfTokens;
    }

    public void register(Javalin app) {
        app.get("/admin", context -> {
            if (requireAdmin(context) == null) {
                return;
            }
            render(context, null);
        });

        app.post("/admin/products", context -> {
            csrfTokens.verify(context);
            AuthUser user = requireAdmin(context);
            if (user == null) {
                return;
            }
            try {
                productService.create(new CreateProductRequest(
                        context.formParam("title"),
                        context.formParam("description"),
                        decimal(context.formParam("price"), "price"),
                        decimal(context.formParam("rating"), "rating"),
                        images(context.formParam("images")),
                        integer(context.formParam("stockQuantity"), "stockQuantity", 0),
                        null
                ));
                context.redirect("/admin?created=1");
            } catch (ApiException exception) {
                render(context, exception.getMessage());
            }
        });

        app.post("/admin/products/{id}/delete", context -> {
            csrfTokens.verify(context);
            if (requireAdmin(context) == null) {
                return;
            }
            productService.delete(context.pathParam("id"));
            context.redirect("/admin?deleted=1");
        });

        app.post("/admin/media/preview", context -> {
            csrfTokens.verify(context);
            if (requireAdmin(context) == null) {
                return;
            }
            UploadedFile file = context.uploadedFile("file");
            try {
                mediaService.uploadPreview(file);
                context.redirect("/admin?uploaded=preview");
            } catch (ApiException exception) {
                render(context, exception.getMessage());
            }
        });

        app.post("/admin/media/dataset/{productId}", context -> {
            csrfTokens.verify(context);
            if (requireAdmin(context) == null) {
                return;
            }
            UploadedFile file = context.uploadedFile("file");
            try {
                mediaService.uploadDataset(context.pathParam("productId"), file);
                context.redirect("/admin?uploaded=dataset");
            } catch (ApiException exception) {
                render(context, exception.getMessage());
            }
        });

        app.post("/admin/media/dataset", context -> {
            csrfTokens.verify(context);
            if (requireAdmin(context) == null) {
                return;
            }
            UploadedFile file = context.uploadedFile("file");
            try {
                mediaService.uploadDataset(context.formParam("productId"), file);
                context.redirect("/admin?uploaded=dataset");
            } catch (ApiException exception) {
                render(context, exception.getMessage());
            }
        });
    }

    private AuthUser requireAdmin(Context context) {
        return PageSupport.requireAdmin(context, requestAuth, cartService, csrfTokens, "admin");
    }

    private void render(Context context, String error) {
        var result = productService.list(ProductQuery.builder().limit(100).build());
        context.attribute("products", result.response().getItems());
        context.attribute("error", error);
        context.attribute("created", context.queryParam("created") != null);
        context.attribute("deleted", context.queryParam("deleted") != null);
        context.attribute("uploaded", context.queryParam("uploaded"));
        PageSupport.render(context, "admin");
    }

    private BigDecimal decimal(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException exception) {
            throw com.datashop.web.Errors.badRequest(field + " must be a number");
        }
    }

    private Integer integer(String raw, String field, int fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException exception) {
            throw com.datashop.web.Errors.badRequest(field + " must be an integer");
        }
    }

    private List<String> images(String raw) {
        if (raw == null) {
            return null;
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }
}
