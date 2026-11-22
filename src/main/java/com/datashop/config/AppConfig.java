package com.datashop.config;

import com.datashop.util.Constants;
import com.datashop.util.config.Durations;
import com.datashop.util.config.Env;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
@ToString
public final class AppConfig {

    private final int port;
    private final String jwtSecret;
    private final Duration jwtTtl;
    private final boolean secureCookies;
    private final Set<String> corsOrigins;

    private final DatabaseSettings database;
    private final String redisUrl;
    private final String supabaseUrl;
    private final String supabasePublishableKey;
    private final String supabaseServiceRoleKey;
    private final String supabaseStorageEmail;
    private final String supabaseStoragePassword;
    private final int cacheTtlSeconds;

    private final long previewMaxBytes;
    private final long datasetMaxBytes;

    private final String seedAdminEmail;
    private final String seedAdminPassword;
    private final boolean seedEnabled;

    public static AppConfig load() {
        int port = Env.getInt(Constants.EnvKeys.PORT, Constants.Config.DEFAULT_PORT);
        String renderUrl = Env.get(Constants.EnvKeys.RENDER_EXTERNAL_URL, "");
        Set<String> corsOrigins = Arrays.stream(
                        Env.get(Constants.EnvKeys.CORS_ORIGINS,
                                renderUrl.isBlank()
                                        ? Constants.Config.DEFAULT_CORS_ORIGIN
                                        : renderUrl)
                                .split(Constants.Config.CORS_ORIGINS_SEPARATOR))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return AppConfig.builder()
                .port(port)
                .jwtSecret(Env.get(Constants.EnvKeys.JWT_SECRET, Constants.Config.DEFAULT_JWT_SECRET))
                .jwtTtl(Durations.parse(
                        Env.get(Constants.EnvKeys.JWT_EXPIRES_IN, Constants.Config.DEFAULT_JWT_TTL),
                        Constants.Config.DEFAULT_JWT_TTL_DURATION))
                .secureCookies(Env.getBoolean(Constants.EnvKeys.COOKIE_SECURE, false))
                .corsOrigins(corsOrigins)
                .database(DatabaseSettings.fromEnvironment())
                .redisUrl(Env.get(Constants.EnvKeys.REDIS_URL, Constants.Config.DEFAULT_REDIS_URL))
                .supabaseUrl(Env.get(Constants.EnvKeys.SUPABASE_URL, ""))
                .supabasePublishableKey(Env.get(Constants.EnvKeys.SUPABASE_PUBLISHABLE_KEY, ""))
                .supabaseServiceRoleKey(Env.get(Constants.EnvKeys.SUPABASE_SERVICE_ROLE_KEY, ""))
                .supabaseStorageEmail(Env.get(Constants.EnvKeys.SUPABASE_STORAGE_EMAIL, ""))
                .supabaseStoragePassword(Env.get(Constants.EnvKeys.SUPABASE_STORAGE_PASSWORD, ""))
                .cacheTtlSeconds(Env.getInt(
                        Constants.EnvKeys.CACHE_TTL_SECONDS, Constants.Config.DEFAULT_CACHE_TTL_SECONDS))
                .previewMaxBytes(Env.getLong(
                        Constants.EnvKeys.PREVIEW_MAX_BYTES, Constants.Config.DEFAULT_PREVIEW_MAX_BYTES))
                .datasetMaxBytes(Env.getLong(
                        Constants.EnvKeys.DATASET_MAX_BYTES, Constants.Config.DEFAULT_DATASET_MAX_BYTES))
                .seedAdminEmail(Env.get(
                        Constants.EnvKeys.SEED_ADMIN_EMAIL, Constants.Config.DEFAULT_SEED_ADMIN_EMAIL))
                .seedAdminPassword(Env.get(
                        Constants.EnvKeys.SEED_ADMIN_PASSWORD, Constants.Config.DEFAULT_SEED_ADMIN_PASSWORD))
                .seedEnabled(Env.getBoolean(Constants.EnvKeys.SEED_ENABLED, Constants.Config.DEFAULT_SEED_ENABLED))
                .build();
    }
}
