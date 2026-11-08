package com.datashop.service;

import com.datashop.query.ProductQuery;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class CatalogCacheKeys {

    private CatalogCacheKeys() {
    }

    public static String forQuery(ProductQuery query) {
        String raw = String.join("|",
                nullSafe(query.getSearch()),
                String.valueOf(query.getMinPrice()),
                String.valueOf(query.getMaxPrice()),
                String.valueOf(query.getMinRating()),
                query.getSortBy().requestValue(),
                query.getOrder().value(),
                String.valueOf(query.getPage()),
                String.valueOf(query.getLimit())
        );
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return "catalog:products:" + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-1 is not available", exception);
        }
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
