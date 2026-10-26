package com.datashop.domain;

import java.util.Locale;

public enum UserRole {
    ADMIN("admin"),
    CUSTOMER("customer");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static UserRole from(String raw) {
        if (raw == null || raw.isBlank()) {
            return CUSTOMER;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        for (UserRole role : values()) {
            if (role.value.equals(normalized)) {
                return role;
            }
        }
        return CUSTOMER;
    }

    public static UserRole ofName(String raw) {
        if (raw == null || raw.isBlank()) {
            return CUSTOMER;
        }
        return valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }
}
