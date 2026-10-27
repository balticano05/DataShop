package com.datashop.web.dto;

import com.datashop.query.ProductPage;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ProductsResponse {

    List<ProductView> items;
    ProductsMeta meta;

    @JsonCreator
    public static ProductsResponse create(
            @JsonProperty("items") List<ProductView> items,
            @JsonProperty("meta") ProductsMeta meta
    ) {
        return ProductsResponse.builder().items(items).meta(meta).build();
    }

    public static ProductsResponse from(ProductPage page) {
        return ProductsResponse.builder()
                .items(page.getItems().stream().map(ProductView::from).toList())
                .meta(ProductsMeta.from(page))
                .build();
    }
}
