package com.datashop.service;

import com.datashop.query.ProductQuery;
import com.datashop.query.ProductSortField;
import com.datashop.query.SortOrder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogCacheKeysTest {

    @Test
    void producesStableKeyForSameQuery() {
        ProductQuery query = ProductQuery.builder()
                .search("Tesla")
                .minPrice(new BigDecimal("100"))
                .sortBy(ProductSortField.PRICE)
                .order(SortOrder.ASC)
                .page(2)
                .limit(12)
                .build();

        assertThat(CatalogCacheKeys.forQuery(query))
                .isEqualTo(CatalogCacheKeys.forQuery(query))
                .startsWith("catalog:products:");
    }

    @Test
    void producesDifferentKeysForDifferentQueries() {
        ProductQuery first = ProductQuery.builder().search("Tesla").build();
        ProductQuery second = ProductQuery.builder().search("Weather").build();

        assertThat(CatalogCacheKeys.forQuery(first)).isNotEqualTo(CatalogCacheKeys.forQuery(second));
    }
}
