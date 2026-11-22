package com.datashop.service.media.impl;

import com.datashop.service.media.MediaStorage;
import com.datashop.util.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores private dataset files in Supabase Storage.
 *
 * <p>Product previews are not stored here: their binary data lives in the
 * {@code media_assets} table in Supabase PostgreSQL. Two server-side
 * authentication modes are supported for Storage. A service role key bypasses
 * Storage RLS and can create the bucket. When no service key is available, a
 * dedicated Supabase Auth user signs in and uses RLS policies restricted to
 * that user and the {@code datasets} bucket. Credentials never reach the
 * browser.</p>
 */
public class SupabaseMediaStorage implements MediaStorage {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final long TOKEN_REFRESH_MARGIN_SECONDS = 60;
    private static final int MAX_ERROR_BODY_LENGTH = 300;

    private final String baseUrl;
    private final String apiKey;
    private final String serviceRoleKey;
    private final String storageEmail;
    private final String storagePassword;
    private final boolean canManageBuckets;
    private final String datasetsBucket;
    private final HttpClient httpClient;
    private final Set<String> ensuredBuckets = ConcurrentHashMap.newKeySet();
    private volatile String accessToken;
    private volatile Instant accessTokenExpiresAt = Instant.EPOCH;

    public SupabaseMediaStorage(
            String supabaseUrl,
            String publishableKey,
            String serviceRoleKey,
            String storageEmail,
            String storagePassword
    ) {
        this.baseUrl = stripTrailingSlash(supabaseUrl);
        this.serviceRoleKey = normalizeKey(serviceRoleKey);
        this.apiKey = this.serviceRoleKey == null ? normalizeKey(publishableKey) : this.serviceRoleKey;
        if (this.apiKey == null) {
            throw new IllegalStateException(Constants.Messages.SUPABASE_STORAGE_AUTH_REQUIRED);
        }
        this.storageEmail = normalize(storageEmail);
        this.storagePassword = normalize(storagePassword);
        this.canManageBuckets = this.serviceRoleKey != null;
        this.datasetsBucket = Constants.Media.SUPABASE_DATASETS_BUCKET;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
    }

    @Override
    public StoredMedia store(InputStream data, String originalFilename, String contentType) {
        String filename = sanitizeFilename(originalFilename);
        String objectPath = Constants.Media.DATASETS_FOLDER + "/" + UUID.randomUUID() + "-" + filename;
        String mimeType = contentType == null || contentType.isBlank()
                ? Constants.Media.DEFAULT_CONTENT_TYPE
                : contentType;

        byte[] bytes;
        try {
            bytes = data.readAllBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read uploaded file", exception);
        }

        ensureBucket(datasetsBucket);

        HttpRequest request = requestBuilder(objectUrl(datasetsBucket, objectPath))
                .header("Content-Type", mimeType)
                .header("x-upsert", "false")
                .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                .build();
        HttpResponse<String> response = send(request, "store uploaded file");
        if (!isSuccessful(response)) {
            throw responseError("store uploaded file", response);
        }

        return new StoredMedia(objectPath);
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        HttpRequest request = requestBuilder(objectUrl(datasetsBucket, publicId))
                .DELETE()
                .build();
        HttpResponse<String> response = send(request, "delete stored file");
        if (isSuccessful(response) || response.statusCode() == Constants.Http.Status.NOT_FOUND) {
            return;
        }
        throw responseError("delete stored file", response);
    }

    @Override
    public Optional<String> downloadUrl(String publicId, Duration ttl) {
        if (publicId == null || !publicId.startsWith(Constants.Media.DATASETS_PREFIX)) {
            return Optional.empty();
        }
        long expiresIn = Math.max(1, ttl.toSeconds());
        String body;
        try {
            body = JSON.writeValueAsString(Map.of("expiresIn", expiresIn));
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot build Supabase signed URL request", exception);
        }

        HttpRequest request = requestBuilder(signUrl(datasetsBucket, publicId))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = send(request, "create signed download URL");
        if (!isSuccessful(response)) {
            throw responseError("create signed download URL", response);
        }

        JsonNode payload;
        try {
            payload = JSON.readTree(response.body());
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot parse Supabase signed URL response", exception);
        }
        String signedUrl = firstText(payload, "signedURL", "signedUrl", "signed_url");
        if (signedUrl == null || signedUrl.isBlank()) {
            throw new IllegalStateException("Supabase signed URL response does not contain signedURL");
        }
        if (signedUrl.startsWith("http://") || signedUrl.startsWith("https://")) {
            return Optional.of(signedUrl);
        }
        return Optional.of(baseUrl + "/storage/v1"
                + (signedUrl.startsWith("/") ? signedUrl : "/" + signedUrl));
    }

    private void ensureBucket(String bucket) {
        if (!canManageBuckets || ensuredBuckets.contains(bucket)) {
            return;
        }
        String body;
        try {
            body = JSON.writeValueAsString(Map.of(
                    "id", bucket,
                    "name", bucket,
                    "public", false
            ));
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot build Supabase bucket request", exception);
        }

        HttpRequest create = requestBuilder(baseUrl + "/storage/v1/bucket")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = send(create, "create bucket " + bucket);
        if (isSuccessful(response)) {
            ensuredBuckets.add(bucket);
            return;
        }

        // Supabase returns 400/409 when the bucket already exists. Verify it before
        // treating that response as success, so invalid bucket names still fail fast.
        if (response.statusCode() == Constants.Http.Status.BAD_REQUEST
                || response.statusCode() == Constants.Http.Status.CONFLICT) {
            HttpRequest get = requestBuilder(baseUrl + "/storage/v1/bucket/" + encode(bucket))
                    .GET()
                    .build();
            HttpResponse<String> existing = send(get, "check bucket " + bucket);
            if (isSuccessful(existing)) {
                ensuredBuckets.add(bucket);
                return;
            }
        }
        throw responseError("create bucket " + bucket, response);
    }

    private HttpRequest.Builder requestBuilder(String url) {
        return HttpRequest.newBuilder(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .header("Authorization", "Bearer " + authorizationToken())
                .header("apikey", apiKey);
    }

    private String authorizationToken() {
        if (serviceRoleKey != null) {
            return serviceRoleKey;
        }
        if (storageEmail == null || storagePassword == null) {
            throw new IllegalStateException(Constants.Messages.SUPABASE_STORAGE_AUTH_REQUIRED);
        }
        String token = accessToken;
        if (token != null && Instant.now().isBefore(
                accessTokenExpiresAt.minusSeconds(TOKEN_REFRESH_MARGIN_SECONDS))) {
            return token;
        }
        return refreshAccessToken();
    }

    private synchronized String refreshAccessToken() {
        String token = accessToken;
        if (token != null && Instant.now().isBefore(
                accessTokenExpiresAt.minusSeconds(TOKEN_REFRESH_MARGIN_SECONDS))) {
            return token;
        }

        String body;
        try {
            body = JSON.writeValueAsString(Map.of(
                    "email", storageEmail,
                    "password", storagePassword
            ));
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot build Supabase Auth request", exception);
        }

        HttpRequest request = HttpRequest.newBuilder(
                        URI.create(baseUrl + "/auth/v1/token?grant_type=password"))
                .timeout(REQUEST_TIMEOUT)
                .header("apikey", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = send(request, "authenticate Supabase storage user");
        if (!isSuccessful(response)) {
            throw responseError("authenticate Supabase storage user", response);
        }

        JsonNode payload;
        try {
            payload = JSON.readTree(response.body());
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot parse Supabase Auth response", exception);
        }
        String freshToken = firstText(payload, "access_token");
        if (freshToken == null || freshToken.isBlank()) {
            throw new IllegalStateException("Supabase Auth response does not contain access_token");
        }
        long expiresIn = payload.path("expires_in").asLong(3600);
        accessToken = freshToken;
        accessTokenExpiresAt = Instant.now().plusSeconds(Math.max(60, expiresIn));
        return freshToken;
    }

    private HttpResponse<String> send(HttpRequest request, String action) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Supabase Storage request was interrupted: " + action, exception);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot reach Supabase Storage: " + action, exception);
        }
    }

    private String objectUrl(String bucket, String objectPath) {
        return baseUrl + "/storage/v1/object/" + encode(bucket) + "/" + encodePath(objectPath);
    }

    private String signUrl(String bucket, String objectPath) {
        return baseUrl + "/storage/v1/object/sign/" + encode(bucket) + "/" + encodePath(objectPath);
    }

    private static boolean isSuccessful(HttpResponse<String> response) {
        return response.statusCode() >= 200 && response.statusCode() < 300;
    }

    private static IllegalStateException responseError(String action, HttpResponse<String> response) {
        String body = Optional.ofNullable(response.body())
                .orElse("")
                .replaceAll("\\s+", " ")
                .trim();
        if (body.length() > MAX_ERROR_BODY_LENGTH) {
            body = body.substring(0, MAX_ERROR_BODY_LENGTH);
        }
        String suffix = body.isEmpty() ? "" : " - " + body;
        return new IllegalStateException(
                "Cannot " + action + " in Supabase: HTTP " + response.statusCode() + suffix);
    }

    private static String firstText(JsonNode node, String... names) {
        for (String name : names) {
            JsonNode value = node.get(name);
            if (value != null && !value.isNull()) {
                return value.asText();
            }
        }
        return null;
    }

    private static String encodePath(String path) {
        String[] segments = path.split("/", -1);
        StringBuilder encoded = new StringBuilder();
        for (int index = 0; index < segments.length; index++) {
            if (index > 0) {
                encoded.append('/');
            }
            encoded.append(encode(segments[index]));
        }
        return encoded.toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return Constants.Media.DEFAULT_FILENAME;
        }
        String clean = java.nio.file.Path.of(filename).getFileName().toString()
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        return clean.length() > Constants.Media.MAX_FILENAME_LENGTH
                ? clean.substring(clean.length() - Constants.Media.MAX_FILENAME_LENGTH)
                : clean;
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String normalizeKey(String value) {
        String normalized = normalize(value);
        return normalized == null || "placeholder".equalsIgnoreCase(normalized) ? null : normalized;
    }

    private static String stripTrailingSlash(String url) {
        String value = url == null ? "" : url.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
