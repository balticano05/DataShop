package com.datashop.query;

public enum SortOrder {
    ASC,
    DESC;

    public String value() {
        return name().toLowerCase();
    }

    public static SortOrder from(String raw) {
        if (raw == null || raw.isBlank()) {
            return DESC;
        }
        for (SortOrder order : values()) {
            if (order.name().equalsIgnoreCase(raw.trim())) {
                return order;
            }
        }
        throw new IllegalArgumentException("Unsupported order value: " + raw);
    }
}
