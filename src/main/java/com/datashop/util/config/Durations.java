package com.datashop.util.config;

import com.datashop.util.Constants;
import lombok.experimental.UtilityClass;

import java.time.Duration;

@UtilityClass
public final class Durations {

    public static Duration parse(String raw, Duration fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        String value = raw.trim().toLowerCase();
        if (value.startsWith(Constants.Config.DURATION_ISO_PREFIX)) {
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
                        "Unsupported duration suffix in " + raw + ", expected "
                                + Constants.Config.DURATION_SUFFIXES);
            };
        }
        try {
            return Duration.ofSeconds(Long.parseLong(value));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid duration: " + raw, exception);
        }
    }
}
