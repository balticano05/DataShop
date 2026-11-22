package com.datashop.web.dto.product;

import java.math.BigDecimal;
import java.util.List;

public record UpdateProductRequest(
        String title,
        String description,
        BigDecimal price,
        BigDecimal rating,
        List<String> images,
        Integer stockQuantity,
        String filePublicId
) {
}
