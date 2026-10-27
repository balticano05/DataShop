package com.datashop.query;

import com.datashop.domain.Product;
import lombok.Value;

import java.util.List;

@Value
public class ProductPage {

    List<Product> items;
    long total;
    int page;
    int limit;

    public int totalPages() {
        return total == 0 ? 0 : (int) Math.ceil((double) total / limit);
    }
}
