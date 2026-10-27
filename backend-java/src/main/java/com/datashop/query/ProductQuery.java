package com.datashop.query;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Value
@Builder
public class ProductQuery {

    String search;
    BigDecimal minPrice;
    BigDecimal maxPrice;
    BigDecimal minRating;
    @Builder.Default
    ProductSortField sortBy = ProductSortField.CREATED_AT;
    @Builder.Default
    SortOrder order = SortOrder.DESC;
    @Builder.Default
    int page = 1;
    @Builder.Default
    int limit = 12;

    public int offset() {
        return (page - 1) * limit;
    }

    public Map<String, String> cacheParameters() {
        Map<String, String> values = new LinkedHashMap<>();
        put(values, "search", search);
        put(values, "min_price", minPrice);
        put(values, "max_price", maxPrice);
        put(values, "min_rating", minRating);
        put(values, "sort_by", sortBy.requestValue());
        put(values, "order", order.name().toLowerCase());
        put(values, "page", page);
        put(values, "limit", limit);
        return values;
    }

    private static void put(Map<String, String> values, String key, Object value) {
        if (value != null) {
            values.put(key, String.valueOf(value));
        }
    }
}
