package com.datashop.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.datashop.web.Errors;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CloudinaryMediaStorage implements MediaStorage {

    private final Cloudinary cloudinary;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public CloudinaryMediaStorage(String cloudinaryUrl) {
        this.cloudinary = new Cloudinary(cloudinaryUrl);
    }

    @Override
    public StoredMedia store(InputStream data, String originalFilename, String contentType, String folder) {
        boolean dataset = "datasets".equalsIgnoreCase(folder);
        String safeFolder = sanitizeFolder(folder);
        String publicId = safeFolder + "/" + UUID.randomUUID() + "-" + sanitizeFilename(originalFilename);
        try {
            Map<?, ?> result = cloudinary.uploader().upload(data, ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", dataset ? "raw" : "image",
                    "type", dataset ? "private" : "upload",
                    "overwrite", false,
                    "use_filename", false,
                    "unique_filename", false
            ));
            String storedId = stringValue(result.get("public_id"), publicId);
            String url = stringValue(result.get("secure_url"), "");
            Number bytes = result.get("bytes") instanceof Number number ? number : 0;
            return new StoredMedia(storedId, url, contentType, bytes.longValue());
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot store uploaded file in Cloudinary", exception);
        }
    }

    @Override
    public byte[] read(String publicId) {
        String url = cloudinary.url().secure(true).generate(publicId);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw Errors.notFound("File not found");
            }
            return response.body();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read stored file from Cloudinary", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while reading Cloudinary file", exception);
        }
    }

    @Override
    public boolean exists(String publicId) {
        return publicId != null && !publicId.isBlank();
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        boolean dataset = publicId.startsWith("datasets/");
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                    "resource_type", dataset ? "raw" : "image",
                    "type", dataset ? "private" : "upload",
                    "invalidate", true
            ));
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot delete stored file from Cloudinary: " + publicId, exception);
        }
    }

    @Override
    public String downloadUrl(String publicId, Duration ttl) {
        if (publicId == null || !publicId.startsWith("datasets/")) {
            return null;
        }
        int slash = publicId.lastIndexOf('/');
        int dot = publicId.lastIndexOf('.');
        String id = publicId;
        String format = "bin";
        if (dot > slash) {
            id = publicId.substring(0, dot);
            format = publicId.substring(dot + 1).toLowerCase(Locale.ROOT);
        }
        long expiresAt = Instant.now().plus(ttl).getEpochSecond();
        try {
            return cloudinary.privateDownload(id, format, ObjectUtils.asMap(
                    "resource_type", "raw",
                    "type", "private",
                    "expires_at", expiresAt
            ));
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot create Cloudinary download URL", exception);
        }
    }

    private static String sanitizeFolder(String folder) {
        String normalized = folder == null ? "misc" : folder.replaceAll("[^a-zA-Z0-9_-]", "");
        return normalized.isEmpty() ? "misc" : normalized.toLowerCase(Locale.ROOT);
    }

    private static String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "file";
        }
        String clean = java.nio.file.Path.of(filename).getFileName().toString()
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        return clean.length() > 80 ? clean.substring(clean.length() - 80) : clean;
    }

    private static String stringValue(Object value, String fallback) {
        return value == null ? fallback : value.toString();
    }
}
