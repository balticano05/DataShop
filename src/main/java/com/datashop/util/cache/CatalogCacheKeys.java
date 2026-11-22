package com.datashop.util.cache;

import com.datashop.domain.product.ProductQuery;
import com.datashop.util.Constants;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@UtilityClass
public final class CatalogCacheKeys {

    public static String forQuery(ProductQuery query) {
        String raw = String.join(Constants.Cache.KEY_SEPARATOR,
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
            byte[] digest = MessageDigest.getInstance(Constants.Cache.HASH_ALGORITHM)
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return Constants.Cache.PRODUCTS_PREFIX + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(Constants.Cache.SHA1_UNAVAILABLE_MESSAGE, exception);
        }
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
