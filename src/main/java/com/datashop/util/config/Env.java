package com.datashop.util.config;

import com.datashop.util.Constants;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@UtilityClass
public final class Env {

    private static final Map<String, String> FILE_VALUES = loadFromFiles();

    public static String get(String key) {
        return getOptional(key).orElse(null);
    }

    public static Optional<String> getOptional(String key) {
        return Stream.of(System.getenv(key), System.getProperty(key), FILE_VALUES.get(key))
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }

    public static String get(String key, String defaultValue) {
        return getOptional(key).orElse(defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        return getOptional(key)
                .map(value -> {
                    try {
                        return Integer.parseInt(value.trim());
                    } catch (NumberFormatException exception) {
                        throw new IllegalArgumentException(
                                "Invalid integer value for " + key + ": " + value, exception);
                    }
                })
                .orElse(defaultValue);
    }

    public static long getLong(String key, long defaultValue) {
        return getOptional(key)
                .map(value -> {
                    try {
                        return Long.parseLong(value.trim());
                    } catch (NumberFormatException exception) {
                        throw new IllegalArgumentException(
                                "Invalid long value for " + key + ": " + value, exception);
                    }
                })
                .orElse(defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return getOptional(key)
                .map(value -> switch (value.trim().toLowerCase()) {
                    case "1", "true", "yes", "on" -> true;
                    case "0", "false", "no", "off" -> false;
                    default -> defaultValue;
                })
                .orElse(defaultValue);
    }

    private static Map<String, String> loadFromFiles() {
        Map<String, String> values = new HashMap<>();
        Path candidate = Path.of(Constants.Config.ENV_FILE);
        if (!Files.isRegularFile(candidate)) {
            return Map.of();
        }
        try {
            for (String rawLine : Files.readAllLines(candidate)) {
                parseLine(rawLine, values);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read environment file " + candidate, exception);
        }
        return Map.copyOf(values);
    }

    private static void parseLine(String rawLine, Map<String, String> target) {
        String line = rawLine.trim();
        if (line.isEmpty() || line.startsWith(Constants.Config.ENV_COMMENT_PREFIX)) {
            return;
        }
        if (line.startsWith(Constants.Config.ENV_EXPORT_PREFIX)) {
            line = line.substring(Constants.Config.ENV_EXPORT_PREFIX.length()).trim();
        }
        int equals = line.indexOf(Constants.Config.ENV_ASSIGNMENT);
        if (equals <= 0) {
            return;
        }
        String key = line.substring(0, equals).trim();
        String value = line.substring(equals + 1).trim();
        if (value.length() >= 2
                && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")))) {
            value = value.substring(1, value.length() - 1);
        }
        target.putIfAbsent(key, value);
    }
}
