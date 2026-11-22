package com.datashop.domain.product;

import com.datashop.util.Constants;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Builder
@ToString
public final class ProductQuery {

    private final String search;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final BigDecimal minRating;
    @Builder.Default
    private final ProductSortField sortBy = ProductSortField.CREATED_AT;
    @Builder.Default
    private final SortOrder order = SortOrder.DESC;
    @Builder.Default
    private final int page = Constants.Validation.DEFAULT_PAGE;
    @Builder.Default
    private final int limit = Constants.Validation.DEFAULT_PAGE_LIMIT;

    public int offset() {
        return (page - 1) * limit;
    }
}
