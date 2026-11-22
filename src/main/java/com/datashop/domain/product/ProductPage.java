package com.datashop.domain.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@AllArgsConstructor
public final class ProductPage {

    private final List<Product> items;
    private final long total;
    private final int page;
    private final int limit;

    public int totalPages() {
        return total == 0 ? 0 : (int) Math.ceil((double) total / limit);
    }
}
