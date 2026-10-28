package com.datashop.web.api;

import com.datashop.query.ProductQuery;
import com.datashop.query.ProductSortField;
import com.datashop.query.SortOrder;
import com.datashop.web.Errors;
import io.javalin.http.Context;

import java.math.BigDecimal;

public final class ProductQueryParser {

    private ProductQueryParser() {
    }

    public static ProductQuery parse(Context context) {
        ProductSortField sortBy;
        SortOrder order;
        try {
            sortBy = ProductSortField.from(context.queryParam("sort_by"));
            order = SortOrder.from(context.queryParam("order"));
        } catch (IllegalArgumentException exception) {
            throw Errors.badRequest(exception.getMessage());
        }

        BigDecimal minPrice = decimal(context.queryParam("min_price"), "min_price");
        BigDecimal maxPrice = decimal(context.queryParam("max_price"), "max_price");
        BigDecimal minRating = decimal(context.queryParam("min_rating"), "min_rating");
        if (minPrice != null && minPrice.signum() < 0) {
            throw Errors.badRequest("min_price must be at least 0");
        }
        if (maxPrice != null && maxPrice.signum() < 0) {
            throw Errors.badRequest("max_price must be at least 0");
        }
        if (minRating != null && (minRating.signum() < 0
                || minRating.compareTo(BigDecimal.valueOf(5)) > 0)) {
            throw Errors.badRequest("min_rating must be between 0 and 5");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw Errors.badRequest("min_price must not exceed max_price");
        }

        String search = blankToNull(context.queryParam("search"));
        if (search != null && search.length() > 100) {
            throw Errors.badRequest("search must be at most 100 characters");
        }

        return ProductQuery.builder()
                .search(search)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minRating(minRating)
                .sortBy(sortBy)
                .order(order)
                .page(integer(context.queryParam("page"), 1, 1, 100_000))
                .limit(integer(context.queryParam("limit"), 12, 1, 100))
                .build();
    }

    private static BigDecimal decimal(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException exception) {
            throw Errors.badRequest(field + " must be a number");
        }
    }

    private static int integer(String raw, int fallback, int min, int max) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            if (value < min || value > max) {
                throw Errors.badRequest("value must be between " + min + " and " + max);
            }
            return value;
        } catch (NumberFormatException exception) {
            throw Errors.badRequest("value must be an integer");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
