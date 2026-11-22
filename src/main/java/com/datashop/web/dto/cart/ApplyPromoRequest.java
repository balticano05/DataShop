package com.datashop.web.dto.cart;

import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

public record ApplyPromoRequest(
        @OpenApiRequired
        @OpenApiExample("SAVE10")
        String code
) {
}
