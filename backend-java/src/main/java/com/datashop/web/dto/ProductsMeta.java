package com.datashop.web.dto;

import com.datashop.query.ProductPage;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductsMeta {

    @JsonProperty("total_items")
    long totalItems;

    @JsonProperty("total_pages")
    int totalPages;

    @JsonProperty("current_page")
    int currentPage;

    int limit;

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
