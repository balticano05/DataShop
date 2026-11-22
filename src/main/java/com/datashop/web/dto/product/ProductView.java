package com.datashop.web.dto.product;

import com.datashop.domain.product.Product;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Getter
@Builder
@ToString
public final class ProductView {

    private final String id;
    private final String title;
    private final String description;
    private final BigDecimal price;
    private final BigDecimal rating;
    private final List<String> images;
    private final int stockQuantity;
    private final Instant createdAt;
    private final Instant updatedAt;

    @JsonCreator
    public static ProductView create(
            @JsonProperty("id") String id,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("rating") BigDecimal rating,
            @JsonProperty("images") List<String> images,
            @JsonProperty("stockQuantity") int stockQuantity,
            @JsonProperty("createdAt") Instant createdAt,
            @JsonProperty("updatedAt") Instant updatedAt
    ) {
        return ProductView.builder()
                .id(id)
                .title(title)
                .description(description)
                .price(price)
                .rating(rating)
                .images(images)
                .stockQuantity(stockQuantity)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static ProductView from(Product product) {
        return ProductView.builder()
                .id(product.getId().toString())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .rating(product.getRating())
                .images(Optional.ofNullable(product.getImages())
                        .map(List::copyOf)
                        .orElseGet(List::of))
                .stockQuantity(product.getStockQuantity())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
