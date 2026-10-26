package com.datashop.config;

import java.time.Duration;

public final class Durations {

    private Durations() {
    }

    public static Duration parse(String raw, Duration fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        String value = raw.trim().toLowerCase();
        if (value.startsWith("p")) {
            try {
                return Duration.parse(raw.trim().toUpperCase());
            } catch (RuntimeException exception) {
                throw new IllegalArgumentException("Invalid duration: " + raw, exception);
            }
        }
        char suffix = value.charAt(value.length() - 1);
        if (!Character.isDigit(suffix)) {
            String numberPart = value.substring(0, value.length() - 1);
            long amount;
            try {
                amount = Long.parseLong(numberPart);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid duration: " + raw, exception);
            }
            return switch (suffix) {
                case 's' -> Duration.ofSeconds(amount);
                case 'm' -> Duration.ofMinutes(amount);
                case 'h' -> Duration.ofHours(amount);
                case 'd' -> Duration.ofDays(amount);
                default -> throw new IllegalArgumentException(
                        "Unsupported duration suffix in " + raw + ", expected s, m, h or d");
            };
        }
        try {
            return Duration.ofSeconds(Long.parseLong(value));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid duration: " + raw, exception);
        }
    }
}
