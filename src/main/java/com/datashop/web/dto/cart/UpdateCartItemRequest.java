package com.datashop.web.dto.cart;

import io.javalin.openapi.OpenApiRequired;

public record UpdateCartItemRequest(
        @OpenApiRequired
        Integer quantity
) {
}
