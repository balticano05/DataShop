package com.datashop.web.dto.product;

import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequired;

import java.math.BigDecimal;
import java.util.List;

public record CreateProductRequest(
        @OpenApiRequired
        @OpenApiExample("Urban Mobility Dataset")
        String title,
        @OpenApiRequired
        @OpenApiExample("Исторические данные о поездках и загруженности улиц")
        String description,
        @OpenApiRequired
        BigDecimal price,
        BigDecimal rating,
        @OpenApiExample("Optional: /media/assets/00000000-0000-0000-0000-000000000000")
        List<String> images,
        @OpenApiRequired
        Integer stockQuantity,
        String filePublicId
) {
}
