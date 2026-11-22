package com.datashop.domain.user;

import lombok.RequiredArgsConstructor;

import java.util.Locale;

@RequiredArgsConstructor
public enum UserRole {
    ADMIN("admin"),
    CUSTOMER("customer");

    private final String value;

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
}
