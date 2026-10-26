package com.datashop.config;

import lombok.Builder;
import lombok.Value;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Value
@Builder
public class AppConfig {

    int port;
    String jwtSecret;
    Duration jwtTtl;
    boolean secureCookies;
    Set<String> corsOrigins;

    DatabaseSettings database;
    String redisUrl;
    String cloudinaryUrl;
    int cacheTtlSeconds;

    String uploadsDirectory;
    String publicBaseUrl;
    long previewMaxBytes;
    long datasetMaxBytes;

    String seedAdminEmail;
    String seedAdminPassword;
    boolean seedEnabled;

    public static AppConfig load() {
        int port = Env.getInt("PORT", 3000);
        String renderUrl = Env.get("RENDER_EXTERNAL_URL", "");
        String publicBaseUrl = Env.get("PUBLIC_BASE_URL",
                renderUrl.isBlank() ? "http://localhost:" + port : renderUrl);
        Set<String> corsOrigins = Arrays.stream(
                        Env.get("CORS_ORIGINS", renderUrl.isBlank() ? "http://localhost:5173" : renderUrl).split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return AppConfig.builder()
                .port(port)
                .jwtSecret(Env.get("JWT_SECRET", "change-me-datashop-development-secret"))
                .jwtTtl(Durations.parse(Env.get("JWT_EXPIRES_IN", "1h"), Duration.ofHours(1)))
                .secureCookies(Env.getBoolean("COOKIE_SECURE", false))
                .corsOrigins(corsOrigins)
                .database(DatabaseSettings.fromEnvironment())
                .redisUrl(Env.get("REDIS_URL", "redis://localhost:6379"))
                .cloudinaryUrl(Env.get("CLOUDINARY_URL", ""))
                .cacheTtlSeconds(Env.getInt("CACHE_TTL_SECONDS", 90))
                .uploadsDirectory(Env.get("UPLOADS_DIR", "uploads"))
                .publicBaseUrl(publicBaseUrl.endsWith("/")
                        ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                        : publicBaseUrl)
                .previewMaxBytes(Env.getLong("PREVIEW_MAX_BYTES", 5L * 1024 * 1024))
                .datasetMaxBytes(Env.getLong("DATASET_MAX_BYTES", 50L * 1024 * 1024))
                .seedAdminEmail(Env.get("SEED_ADMIN_EMAIL", "admin@datashop.local"))
                .seedAdminPassword(Env.get("SEED_ADMIN_PASSWORD", "Admin12345!"))
                .seedEnabled(Env.getBoolean("SEED_ENABLED", true))
                .build();
    }
}
