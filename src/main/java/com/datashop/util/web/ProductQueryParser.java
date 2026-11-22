package com.datashop.util.web;

import com.datashop.domain.product.ProductQuery;
import com.datashop.domain.product.ProductSortField;
import com.datashop.domain.product.SortOrder;
import com.datashop.util.Constants;
import io.javalin.http.Context;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.Optional;

@UtilityClass
public final class ProductQueryParser {

    public static ProductQuery parse(Context context) {
        ProductSortField sortBy;
        SortOrder order;
        try {
            sortBy = ProductSortField.from(context.queryParam(Constants.Params.SORT_BY));
            order = SortOrder.from(context.queryParam(Constants.Params.ORDER));
        } catch (IllegalArgumentException exception) {
            throw Errors.badRequest(exception.getMessage());
        }

        BigDecimal minPrice = decimal(context.queryParam(Constants.Params.MIN_PRICE), Constants.Params.MIN_PRICE);
        BigDecimal maxPrice = decimal(context.queryParam(Constants.Params.MAX_PRICE), Constants.Params.MAX_PRICE);
        BigDecimal minRating = decimal(
                context.queryParam(Constants.Params.MIN_RATING), Constants.Params.MIN_RATING);
        if (minPrice != null && minPrice.signum() < 0) {
            throw Errors.badRequest(
                    Constants.Messages.FIELD_MIN.formatted(Constants.Params.MIN_PRICE, 0));
        }
        if (maxPrice != null && maxPrice.signum() < 0) {
            throw Errors.badRequest(
                    Constants.Messages.FIELD_MIN.formatted(Constants.Params.MAX_PRICE, 0));
        }
        if (minRating != null && (minRating.signum() < 0
                || minRating.compareTo(Constants.Validation.MAX_RATING) > 0)) {
            throw Errors.badRequest(Constants.Messages.FIELD_BETWEEN.formatted(
                    Constants.Params.MIN_RATING,
                    0,
                    Constants.Validation.MAX_RATING.toPlainString()));
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw Errors.badRequest(Constants.Messages.FIELD_NOT_EXCEED.formatted(
                    Constants.Params.MIN_PRICE, Constants.Params.MAX_PRICE));
        }

        String search = blankToNull(context.queryParam(Constants.Params.SEARCH));
        if (search != null && search.length() > Constants.Validation.SEARCH_MAX_LENGTH) {
            throw Errors.badRequest(Constants.Messages.FIELD_MAX_CHARACTERS.formatted(
                    Constants.Params.SEARCH, Constants.Validation.SEARCH_MAX_LENGTH));
        }

        return ProductQuery.builder()
                .search(search)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minRating(minRating)
                .sortBy(sortBy)
                .order(order)
                .page(integer(
                        context.queryParam(Constants.Params.PAGE),
                        Constants.Validation.DEFAULT_PAGE,
                        Constants.Validation.MIN_PAGE,
                        Constants.Validation.MAX_PAGE))
                .limit(integer(
                        context.queryParam(Constants.Params.LIMIT),
                        Constants.Validation.DEFAULT_PAGE_LIMIT,
                        Constants.Validation.MIN_PAGE_LIMIT,
                        Constants.Validation.MAX_PAGE_LIMIT))
                .build();
    }

    private static BigDecimal decimal(String raw, String field) {
        return Optional.ofNullable(raw)
                .filter(value -> !value.isBlank())
                .map(value -> {
                    try {
                        return new BigDecimal(value.trim());
                    } catch (NumberFormatException exception) {
                        throw Errors.badRequest(Constants.Messages.FIELD_NUMBER.formatted(field));
                    }
                })
                .orElse(null);
    }

    private static int integer(String raw, int fallback, int min, int max) {
        return Optional.ofNullable(raw)
                .filter(value -> !value.isBlank())
                .map(value -> {
                    try {
                        int parsed = Integer.parseInt(value.trim());
                        if (parsed < min || parsed > max) {
                            throw Errors.badRequest(
                                    Constants.Messages.FIELD_BETWEEN.formatted(
                                            Constants.Params.VALUE, min, max));
                        }
                        return parsed;
                    } catch (NumberFormatException exception) {
                        throw Errors.badRequest(
                                Constants.Messages.FIELD_INTEGER.formatted(Constants.Params.VALUE));
                    }
                })
                .orElse(fallback);
    }

    private static String blankToNull(String value) {
        return Optional.ofNullable(value)
                .filter(raw -> !raw.isBlank())
                .map(String::trim)
                .orElse(null);
    }
}
