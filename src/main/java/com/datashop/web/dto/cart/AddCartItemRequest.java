package com.datashop.web.dto.cart;

import io.javalin.openapi.OpenApiRequired;

public record AddCartItemRequest(
        @OpenApiRequired
        String productId,
        Integer quantity
) {
}
