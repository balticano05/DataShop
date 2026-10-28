package com.datashop.web.dto;

import com.datashop.domain.Product;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Value
@Builder
public class ProductView {

    String id;
    String title;
    String description;
    BigDecimal price;
    BigDecimal rating;
    List<String> images;
    int stockQuantity;
    Instant createdAt;
    Instant updatedAt;

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
                .images(List.copyOf(product.getImages()))
                .stockQuantity(product.getStockQuantity())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
