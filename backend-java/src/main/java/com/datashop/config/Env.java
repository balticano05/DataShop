package com.datashop.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Env {

    private static final Map<String, String> FILE_VALUES = loadFromFiles();

    private Env() {
    }

    public static String get(String key) {
        String fromEnv = System.getenv(key);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        String fromProperty = System.getProperty(key);
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }
        return FILE_VALUES.get(key);
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid integer value for " + key + ": " + value, exception);
        }
    }

    public static long getLong(String key, long defaultValue) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid long value for " + key + ": " + value, exception);
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return switch (value.trim().toLowerCase()) {
            case "1", "true", "yes", "on" -> true;
            case "0", "false", "no", "off" -> false;
            default -> defaultValue;
        };
    }

    private static Map<String, String> loadFromFiles() {
        Map<String, String> values = new HashMap<>();
        List<Path> candidates = List.of(
                Path.of(".env"),
                Path.of("../.env"),
                Path.of("backend-java/.env")
        );
        for (Path candidate : candidates) {
            if (!Files.isRegularFile(candidate)) {
                continue;
            }
            try {
                for (String rawLine : Files.readAllLines(candidate)) {
                    parseLine(rawLine, values);
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Cannot read environment file " + candidate, exception);
            }
        }
        return Map.copyOf(values);
    }

    private static void parseLine(String rawLine, Map<String, String> target) {
        String line = rawLine.trim();
        if (line.isEmpty() || line.startsWith("#")) {
            return;
        }
        if (line.startsWith("export ")) {
            line = line.substring("export ".length()).trim();
        }
        int equals = line.indexOf('=');
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
