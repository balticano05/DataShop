package com.datashop.util;

import com.datashop.web.Errors;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Validation {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private Validation() {
    }

    public static String email(String raw) {
        String value = text(raw, "email", 3, 255);
        if (!EMAIL.matcher(value).matches()) {
            throw Errors.badRequest("email must be a valid email address");
        }
        return value.toLowerCase(Locale.ROOT);
    }

    public static String password(String raw, boolean register) {
        if (raw == null || raw.isBlank()) {
            throw Errors.badRequest(register ? "password is required (8-72 characters)" : "password is required");
        }
        if (register && (raw.length() < 8 || raw.length() > 72)) {
            throw Errors.badRequest("password must be between 8 and 72 characters");
        }
        return raw;
    }

    public static String text(String raw, String field, int min, int max) {
        if (raw == null) {
            throw Errors.badRequest(field + " is required");
        }
        String value = raw.trim();
        if (value.length() < min || value.length() > max) {
            throw Errors.badRequest(field + " must be between " + min + " and " + max + " characters");
        }
        return value;
    }

    public static String textMin(String raw, String field, int min) {
        if (raw == null) {
            throw Errors.badRequest(field + " is required");
        }
        String value = raw.trim();
        if (value.length() < min) {
            throw Errors.badRequest(field + " must be at least " + min + " characters");
        }
        return value;
    }

    public static String optionalText(String raw, String field, int min, int max) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.isEmpty()) {
            return null;
        }
        if (value.length() < min || value.length() > max) {
            throw Errors.badRequest(field + " must be between " + min + " and " + max + " characters");
        }
        return value;
    }

    public static BigDecimal decimal(String raw, String field, BigDecimal min, BigDecimal max) {
        if (raw == null || raw.isBlank()) {
            throw Errors.badRequest(field + " is required");
        }
        try {
            BigDecimal value = new BigDecimal(raw.trim());
            return decimal(value, field, min, max);
        } catch (NumberFormatException exception) {
            throw Errors.badRequest(field + " must be a number");
        }
    }

    public static BigDecimal decimal(BigDecimal value, String field, BigDecimal min, BigDecimal max) {
        if (value == null) {
            throw Errors.badRequest(field + " is required");
        }
        if (min != null && value.compareTo(min) < 0) {
            throw Errors.badRequest(field + " must be at least " + min);
        }
        if (max != null && value.compareTo(max) > 0) {
            throw Errors.badRequest(field + " must be at most " + max);
        }
        return value;
    }

    public static BigDecimal optionalDecimal(String raw, String field, BigDecimal min, BigDecimal max) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return decimal(raw, field, min, max);
    }

    public static int integer(String raw, String field, int min, int max) {
        if (raw == null || raw.isBlank()) {
            throw Errors.badRequest(field + " is required");
        }
        try {
            return integer(Integer.parseInt(raw.trim()), field, min, max);
        } catch (NumberFormatException exception) {
            throw Errors.badRequest(field + " must be an integer");
        }
    }

    public static int integer(Integer value, String field, int min, int max) {
        if (value == null) {
            throw Errors.badRequest(field + " is required");
        }
        if (value < min || value > max) {
            throw Errors.badRequest(field + " must be between " + min + " and " + max);
        }
        return value;
    }

    public static Integer optionalInteger(String raw, String field, int min, int max) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return integer(raw, field, min, max);
    }

    public static Integer optionalInteger(Integer value, String field, int min, int max) {
        if (value == null) {
            return null;
        }
        return integer(value, field, min, max);
    }

    public static List<String> imageUrls(List<String> values) {
        if (values == null || values.size() < 3) {
            throw Errors.badRequest("images must contain at least 3 URLs");
        }
        List<String> normalized = new ArrayList<>();
        for (String raw : values) {
            if (raw == null || raw.isBlank()) {
                throw Errors.badRequest("images must not contain blank URLs");
            }
            String value = raw.trim();
            try {
                URI uri = URI.create(value);
                if (uri.getScheme() == null
                        || !(uri.getScheme().equalsIgnoreCase("http") || uri.getScheme().equalsIgnoreCase("https"))) {
                    throw new IllegalArgumentException("unsupported scheme");
                }
                normalized.add(value);
            } catch (IllegalArgumentException exception) {
                throw Errors.badRequest("images must contain valid http(s) URLs");
            }
        }
        return normalized;
    }

    public static String publicId(String raw) {
        return optionalText(raw, "filePublicId", 1, 255);
    }
}
