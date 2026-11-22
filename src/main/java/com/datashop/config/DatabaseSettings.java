package com.datashop.config;

import com.datashop.util.Constants;
import com.datashop.util.config.Env;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Getter
@Builder
@ToString
public final class DatabaseSettings {

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final int maxPoolSize;

    public static DatabaseSettings fromEnvironment() {
        String rawUrl = Env.get(Constants.EnvKeys.DATABASE_URL);
        int maxPoolSize = Env.getInt(
                Constants.EnvKeys.DB_POOL_SIZE, Constants.Database.DEFAULT_POOL_SIZE);

        if (rawUrl == null || rawUrl.isBlank()) {
            throw new IllegalStateException(Constants.Database.URL_REQUIRED);
        }

        URI uri;
        try {
            uri = URI.create(rawUrl);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(Constants.Database.SCHEME_ERROR, exception);
        }
        if (!Constants.Database.POSTGRES_SCHEME.equals(uri.getScheme())
                && !Constants.Database.POSTGRESQL_SCHEME.equals(uri.getScheme())) {
            throw new IllegalStateException(Constants.Database.SCHEME_ERROR);
        }

        String host = uri.getHost();
        String path = uri.getPath();
        if (host == null || host.isBlank()
                || path == null || path.isBlank()
                || uri.getRawUserInfo() == null) {
            throw new IllegalStateException(Constants.Database.URL_INVALID);
        }
        if (!host.toLowerCase(Locale.ROOT)
                .contains(Constants.Supabase.HOST_MARKER)) {
            throw new IllegalStateException(Constants.Database.SUPABASE_ONLY);
        }

        String[] credentials = uri.getRawUserInfo().split(
                Constants.Database.USER_INFO_SEPARATOR, Constants.Database.USER_INFO_LIMIT);
        String username = decode(credentials[0]);
        String password = credentials.length > 1 ? decode(credentials[1]) : "";
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalStateException(Constants.Database.CREDENTIALS_REQUIRED);
        }

        int port = uri.getPort() > 0 ? uri.getPort() : Constants.Database.DEFAULT_PORT_NUMBER;
        String query = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();

        return DatabaseSettings.builder()
                .jdbcUrl(Constants.Database.JDBC_URL_PREFIX + host + ":" + port + path + query)
                .username(username)
                .password(password)
                .maxPoolSize(maxPoolSize)
                .build();
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
