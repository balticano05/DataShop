package com.datashop.util;

import com.datashop.web.Errors;

import java.util.UUID;

public final class Uuids {

    private Uuids() {
    }

    public static UUID parse(String value) {
        if (value == null || value.isBlank()) {
            throw Errors.badRequest("Identifier is required");
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException exception) {
            throw Errors.badRequest("Invalid identifier: " + value);
        }
    }

    public static UUID parseOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parse(value);
    }
}
