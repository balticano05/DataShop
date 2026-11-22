package com.datashop.web.dto.product;

import com.datashop.domain.product.ProductPage;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public final class ProductsResponse {

    private final List<ProductView> items;
    private final ProductsMeta meta;

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
