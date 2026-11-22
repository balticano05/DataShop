package com.datashop.web.page.admin;

import com.datashop.domain.product.ProductQuery;
import com.datashop.domain.user.AuthUser;
import com.datashop.security.CsrfService;
import com.datashop.security.RequestAuthenticator;
import com.datashop.service.cart.CartService;
import com.datashop.service.catalog.ProductService;
import com.datashop.service.media.MediaService;
import com.datashop.util.Constants;
import com.datashop.util.web.Errors;
import com.datashop.util.web.PageSupport;
import com.datashop.web.ApiException;
import com.datashop.web.dto.media.UploadedAssetView;
import com.datashop.web.dto.product.CreateProductRequest;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class AdminPageController {

    private final ProductService productService;
    private final MediaService mediaService;
    private final CartService cartService;
    private final RequestAuthenticator requestAuth;
    private final CsrfService csrfService;

    public void register(Javalin app) {
        app.get(Constants.Routes.ADMIN, context -> {
            if (requireAdmin(context).isEmpty()) {
                return;
            }
            render(context, null);
        });

        app.post(Constants.Routes.ADMIN_PRODUCTS, context -> {
            csrfService.verify(context);
            if (requireAdmin(context).isEmpty()) {
                return;
            }
            try {
                productService.create(new CreateProductRequest(
                        context.formParam(Constants.Params.TITLE),
                        context.formParam(Constants.Params.DESCRIPTION),
                        decimal(context.formParam(Constants.Params.PRICE), Constants.Params.PRICE),
                        decimal(context.formParam(Constants.Params.RATING), Constants.Params.RATING),
                        images(context.formParam(Constants.Params.IMAGES)),
                        integer(
                                context.formParam(Constants.Params.STOCK_QUANTITY),
                                Constants.Params.STOCK_QUANTITY,
                                Constants.Validation.MIN_STOCK),
                        null
                ));
                context.redirect(Constants.Redirects.ADMIN_CREATED);
            } catch (ApiException exception) {
                render(context, exception.getMessage());
            }
        });

        app.post(Constants.Routes.ADMIN_PRODUCT_DELETE, context -> {
            csrfService.verify(context);
            if (requireAdmin(context).isEmpty()) {
                return;
            }
            productService.delete(context.pathParam(Constants.Params.ID));
            context.redirect(Constants.Redirects.ADMIN_DELETED);
        });

        app.post(Constants.Routes.ADMIN_MEDIA_PREVIEW, context -> {
            csrfService.verify(context);
            if (requireAdmin(context).isEmpty()) {
                return;
            }
            UploadedFile file = context.uploadedFile(Constants.Params.FILE);
            try {
                UploadedAssetView preview = mediaService.uploadPreview(file);
                context.redirect(Constants.Redirects.ADMIN_PREVIEW_UPLOADED
                        + "&" + Constants.Params.PREVIEW_URL + "="
                        + URLEncoder.encode(preview.getUrl(), StandardCharsets.UTF_8));
            } catch (ApiException exception) {
                render(context, exception.getMessage());
            }
        });

        app.post(Constants.Routes.ADMIN_MEDIA_DATASET, context -> {
            csrfService.verify(context);
            if (requireAdmin(context).isEmpty()) {
                return;
            }
            UploadedFile file = context.uploadedFile(Constants.Params.FILE);
            try {
                mediaService.uploadDataset(context.pathParam(Constants.Params.PRODUCT_ID), file);
                context.redirect(Constants.Redirects.ADMIN_DATASET_UPLOADED);
            } catch (ApiException exception) {
                render(context, exception.getMessage());
            }
        });

        app.post(Constants.Routes.ADMIN_MEDIA_DATASET_FORM, context -> {
            csrfService.verify(context);
            if (requireAdmin(context).isEmpty()) {
                return;
            }
            UploadedFile file = context.uploadedFile(Constants.Params.FILE);
            try {
                mediaService.uploadDataset(context.formParam(Constants.Params.PRODUCT_ID), file);
                context.redirect(Constants.Redirects.ADMIN_DATASET_UPLOADED);
            } catch (ApiException exception) {
                render(context, exception.getMessage());
            }
        });
    }

    private Optional<AuthUser> requireAdmin(Context context) {
        return PageSupport.requireAdmin(
                context, requestAuth, cartService, csrfService, Constants.Messages.ADMIN_PANEL_ACTIVE);
    }

    private void render(Context context, String error) {
        var result = productService.list(ProductQuery.builder()
                .limit(Constants.Validation.ADMIN_PRODUCTS_LIMIT)
                .build());
        context.attribute(Constants.Attributes.PRODUCTS, result.response().getItems());
        context.attribute(Constants.Attributes.ERROR, error);
        context.attribute(
                Constants.Attributes.CREATED,
                context.queryParam(Constants.Params.CREATED) != null);
        context.attribute(
                Constants.Attributes.DELETED,
                context.queryParam(Constants.Params.DELETED) != null);
        context.attribute(Constants.Attributes.UPLOADED, context.queryParam(Constants.Params.UPLOADED));
        context.attribute(
                Constants.Attributes.PREVIEW_URL,
                context.queryParam(Constants.Params.PREVIEW_URL));
        PageSupport.render(context, Constants.Templates.ADMIN);
    }

    private static BigDecimal decimal(String raw, String field) {
        return Optional.ofNullable(raw)
                .filter(value -> !value.isBlank())
                .map(value -> {
                    try {
                        return new BigDecimal(value.trim());
                    } catch (NumberFormatException exception) {
                        throw Errors.badRequest(
                                Constants.Messages.FIELD_NUMBER.formatted(field));
                    }
                })
                .orElse(null);
    }

    private static int integer(String raw, String field, int fallback) {
        return Optional.ofNullable(raw)
                .filter(value -> !value.isBlank())
                .map(value -> {
                    try {
                        return Integer.parseInt(value.trim());
                    } catch (NumberFormatException exception) {
                        throw Errors.badRequest(
                                Constants.Messages.FIELD_INTEGER.formatted(field));
                    }
                })
                .orElse(fallback);
    }

    private static List<String> images(String raw) {
        return Optional.ofNullable(raw)
                .map(value -> Arrays.stream(value.split(Constants.Validation.IMAGES_SEPARATOR))
                        .map(String::trim)
                        .filter(entry -> !entry.isEmpty())
                        .toList())
                .orElse(null);
    }
}
