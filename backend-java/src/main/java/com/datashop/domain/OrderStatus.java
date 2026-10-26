package com.datashop.domain;

import java.util.Locale;

public enum OrderStatus {
    PENDING("pending"),
    PAID("paid"),
    CANCELLED("cancelled"),
    REFUNDED("refunded");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static OrderStatus from(String raw) {
        if (raw == null || raw.isBlank()) {
            return PENDING;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        for (OrderStatus status : values()) {
            if (status.value.equals(normalized)) {
                return status;
            }
        }
        return PENDING;
    }
}
