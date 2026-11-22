package com.datashop.util.validation;

import com.datashop.util.Constants;
import com.datashop.util.web.Errors;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@UtilityClass
public final class Validation {

    private static final Pattern EMAIL = Pattern.compile(Constants.Validation.EMAIL_PATTERN);

    public static String email(String raw) {
        String value = text(
                raw,
                Constants.Params.EMAIL,
                Constants.Validation.EMAIL_MIN_LENGTH,
                Constants.Validation.EMAIL_MAX_LENGTH);
        if (!EMAIL.matcher(value).matches()) {
            throw Errors.badRequest(Constants.Messages.INVALID_EMAIL);
        }
        return value.toLowerCase(Locale.ROOT);
    }

    public static String password(String raw, boolean register) {
        if (raw == null || raw.isBlank()) {
            throw Errors.badRequest(register
                    ? Constants.Messages.PASSWORD_REQUIRED_REGISTER
                    : Constants.Messages.PASSWORD_REQUIRED);
        }
        if (register && (raw.length() < Constants.Validation.PASSWORD_MIN_LENGTH
                || raw.length() > Constants.Validation.PASSWORD_MAX_LENGTH)) {
            throw Errors.badRequest(Constants.Messages.PASSWORD_LENGTH);
        }
        return raw;
    }

    public static String text(String raw, String field, int min, int max) {
        if (raw == null) {
            throw Errors.badRequest(Constants.Messages.FIELD_REQUIRED.formatted(field));
        }
        String value = raw.trim();
        if (value.length() < min || value.length() > max) {
            throw Errors.badRequest(
                    Constants.Messages.FIELD_BETWEEN_CHARACTERS.formatted(field, min, max));
        }
        return value;
    }

    public static String textMin(String raw, String field, int min) {
        if (raw == null) {
            throw Errors.badRequest(Constants.Messages.FIELD_REQUIRED.formatted(field));
        }
        String value = raw.trim();
        if (value.length() < min) {
            throw Errors.badRequest(
                    Constants.Messages.FIELD_MIN_CHARACTERS.formatted(field, min));
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
            throw Errors.badRequest(
                    Constants.Messages.FIELD_BETWEEN_CHARACTERS.formatted(field, min, max));
        }
        return value;
    }

    public static BigDecimal decimal(BigDecimal value, String field, BigDecimal min, BigDecimal max) {
        if (value == null) {
            throw Errors.badRequest(Constants.Messages.FIELD_REQUIRED.formatted(field));
        }
        if (min != null && value.compareTo(min) < 0) {
            throw Errors.badRequest(Constants.Messages.FIELD_MIN.formatted(field, min));
        }
        if (max != null && value.compareTo(max) > 0) {
            throw Errors.badRequest(Constants.Messages.FIELD_MAX.formatted(field, max));
        }
        return value;
    }

    public static int integer(Integer value, String field, int min, int max) {
        if (value == null) {
            throw Errors.badRequest(Constants.Messages.FIELD_REQUIRED.formatted(field));
        }
        if (value < min || value > max) {
            throw Errors.badRequest(Constants.Messages.FIELD_BETWEEN.formatted(field, min, max));
        }
        return value;
    }

    public static List<String> imageUrls(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>();
        for (String raw : values) {
            if (raw == null || raw.isBlank()) {
                throw Errors.badRequest(Constants.Messages.IMAGES_BLANK_URL);
            }
            String assetId = assetId(raw.trim());
            if (assetId == null) {
                throw Errors.badRequest(Constants.Messages.IMAGES_INVALID_URL);
            }
            String canonical = Constants.Media.ASSET_PATH_PREFIX + assetId;
            if (!normalized.contains(canonical)) {
                normalized.add(canonical);
            }
        }
        if (normalized.size() < Constants.Validation.IMAGES_MIN_COUNT) {
            throw Errors.badRequest(Constants.Messages.IMAGES_MIN_COUNT);
        }
        return normalized;
    }

    private static String assetId(String value) {
        String path = value;
        if (value.startsWith(Constants.Validation.HTTP_SCHEME + "://")
                || value.startsWith(Constants.Validation.HTTPS_SCHEME + "://")) {
            URI uri;
            try {
                uri = URI.create(value);
            } catch (IllegalArgumentException exception) {
                return null;
            }
            if (uri.getHost() == null || uri.getQuery() != null || uri.getFragment() != null) {
                return null;
            }
            path = uri.getPath();
        } else if (value.contains("://") || value.contains("?") || value.contains("#")) {
            return null;
        }
        if (path == null || !path.startsWith(Constants.Media.ASSET_PATH_PREFIX)) {
            return null;
        }
        String assetId = path.substring(Constants.Media.ASSET_PATH_PREFIX.length());
        if (assetId.isBlank() || assetId.contains("/")) {
            return null;
        }
        try {
            UUID.fromString(assetId);
            return assetId;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static String publicId(String raw) {
        return optionalText(
                raw,
                Constants.Params.FILE_PUBLIC_ID,
                Constants.Validation.PUBLIC_ID_MIN_LENGTH,
                Constants.Validation.PUBLIC_ID_MAX_LENGTH);
    }
}
