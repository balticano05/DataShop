package com.datashop.query;

public enum ProductSortField {
    PRICE("price", "price"),
    TITLE("title", "title"),
    RATING("rating", "rating"),
    CREATED_AT("created_at", "createdAt");

    private final String requestValue;
    private final String entityProperty;

    ProductSortField(String requestValue, String entityProperty) {
        this.requestValue = requestValue;
        this.entityProperty = entityProperty;
    }

    public String requestValue() {
        return requestValue;
    }

    public String entityProperty() {
        return entityProperty;
    }

    public static ProductSortField from(String raw) {
        if (raw == null || raw.isBlank()) {
            return CREATED_AT;
        }
        for (ProductSortField field : values()) {
            if (field.requestValue.equalsIgnoreCase(raw.trim())) {
                return field;
            }
        }
        throw new IllegalArgumentException("Unsupported sort_by value: " + raw);
    }
}
