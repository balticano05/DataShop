package com.datashop.web.dto.product;

import com.datashop.domain.product.ProductPage;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.javalin.openapi.OpenApiName;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public final class ProductsMeta {

    @Getter(AccessLevel.NONE)
    @JsonProperty("total_items")
    private final long totalItems;

    @Getter(AccessLevel.NONE)
    @JsonProperty("total_pages")
    private final int totalPages;

    @Getter(AccessLevel.NONE)
    @JsonProperty("current_page")
    private final int currentPage;

    private final int limit;

    @OpenApiName("total_items")
    @JsonProperty("total_items")
    public long getTotalItems() {
        return totalItems;
    }

    @OpenApiName("total_pages")
    @JsonProperty("total_pages")
    public int getTotalPages() {
        return totalPages;
    }

    @OpenApiName("current_page")
    @JsonProperty("current_page")
    public int getCurrentPage() {
        return currentPage;
    }

    @JsonCreator
    public static ProductsMeta create(
            @JsonProperty("total_items") long totalItems,
            @JsonProperty("total_pages") int totalPages,
            @JsonProperty("current_page") int currentPage,
            @JsonProperty("limit") int limit
    ) {
        return ProductsMeta.builder()
                .totalItems(totalItems)
                .totalPages(totalPages)
                .currentPage(currentPage)
                .limit(limit)
                .build();
    }

    public static ProductsMeta from(ProductPage page) {
        return ProductsMeta.builder()
                .totalItems(page.getTotal())
                .totalPages(page.totalPages())
                .currentPage(page.getPage())
                .limit(page.getLimit())
                .build();
    }
}
