package com.datashop.config;

import lombok.Builder;
import lombok.Value;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Value
@Builder
public class DatabaseSettings {

    String jdbcUrl;
    String username;
    String password;
    int maxPoolSize;

    public static DatabaseSettings fromEnvironment() {
        String rawUrl = Env.get("DATABASE_URL");
        String fallbackUser = Env.get("DB_USER", "postgres");
        String fallbackPassword = Env.get("DB_PASSWORD", "postgres");
        int maxPoolSize = Env.getInt("DB_POOL_SIZE", 10);

        if (rawUrl == null || rawUrl.isBlank()) {
            String host = Env.get("DB_HOST", "localhost");
            String port = Env.get("DB_PORT", "5432");
            String database = Env.get("DB_NAME", "shop");
            return DatabaseSettings.builder()
                    .jdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + database)
                    .username(fallbackUser)
                    .password(fallbackPassword)
                    .maxPoolSize(maxPoolSize)
                    .build();
        }

        if (rawUrl.startsWith("jdbc:postgresql:")) {
            return DatabaseSettings.builder()
                    .jdbcUrl(rawUrl)
                    .username(fallbackUser)
                    .password(fallbackPassword)
                    .maxPoolSize(maxPoolSize)
                    .build();
        }

        URI uri = URI.create(rawUrl);
        if (!"postgres".equals(uri.getScheme()) && !"postgresql".equals(uri.getScheme())) {
            throw new IllegalStateException("DATABASE_URL must use the postgres:// or postgresql:// scheme");
        }

        String host = uri.getHost() == null ? "localhost" : uri.getHost();
        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        String path = uri.getPath() == null || uri.getPath().isBlank() ? "/shop" : uri.getPath();
        String query = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();

        String username = fallbackUser;
        String password = fallbackPassword;
        if (uri.getRawUserInfo() != null) {
            String[] parts = uri.getRawUserInfo().split(":", 2);
            username = decode(parts[0]);
            if (parts.length > 1) {
                password = decode(parts[1]);
            }
        }

        return DatabaseSettings.builder()
                .jdbcUrl("jdbc:postgresql://" + host + ":" + port + path + query)
                .username(username)
                .password(password)
                .maxPoolSize(maxPoolSize)
                .build();
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
