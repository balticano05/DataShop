package com.datashop.service;

import com.datashop.config.AppConfig;
import com.datashop.domain.Product;
import com.datashop.web.Errors;
import com.datashop.web.dto.UploadedAssetView;
import io.javalin.http.UploadedFile;
import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
public class MediaService {

    private static final Set<String> PREVIEW_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp"
    );
    private static final Set<String> DATASET_TYPES = Set.of(
            "application/zip",
            "application/x-zip-compressed",
            "application/json",
            "text/csv",
            "text/plain",
            "application/gzip",
            "application/x-gzip",
            "application/octet-stream"
    );

    private final MediaStorage mediaStorage;
    private final ProductService productService;
    private final AppConfig config;

    public UploadedAssetView uploadPreview(UploadedFile file) {
        if (file == null) {
            throw Errors.badRequest("file is required");
        }
        if (file.size() > config.getPreviewMaxBytes()) {
            throw Errors.badRequest("Preview image is too large");
        }
        String contentType = normalizeContentType(file.contentType(), "");
        if (!PREVIEW_TYPES.contains(contentType)) {
            throw Errors.badRequest("Unsupported image type: " + contentType);
        }
        MediaStorage.StoredMedia stored = mediaStorage.store(
                file.content(), file.filename(), contentType, "images");
        return UploadedAssetView.builder()
                .publicId(stored.publicId())
                .url(stored.url())
                .build();
    }

    public byte[] readPreviewImage(String filename) {
        if (filename == null || !filename.matches("[A-Za-z0-9._-]+")) {
            throw Errors.notFound("File not found");
        }
        return mediaStorage.read("images/" + filename);
    }

    public UploadedAssetView uploadDataset(String rawProductId, UploadedFile file) {
        if (file == null) {
            throw Errors.badRequest("file is required");
        }
        if (file.size() > config.getDatasetMaxBytes()) {
            throw Errors.badRequest("Dataset file is too large");
        }
        UUID productId = com.datashop.util.Uuids.parse(rawProductId);
        Product product = productService.requireEntity(productId);

        String contentType = normalizeContentType(file.contentType(), "application/octet-stream");
        if (!DATASET_TYPES.contains(contentType)) {
            throw Errors.badRequest("Unsupported dataset type: " + contentType);
        }
        MediaStorage.StoredMedia stored = mediaStorage.store(
                file.content(), file.filename(), contentType, "datasets");
        productService.setFilePublicId(productId, stored.publicId());
        if (product.getFilePublicId() != null && !product.getFilePublicId().equals(stored.publicId())) {
            mediaStorage.delete(product.getFilePublicId());
        }
        return UploadedAssetView.builder()
                .publicId(stored.publicId())
                .url(stored.url())
                .build();
    }

    private static String normalizeContentType(String raw, String fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        int separator = raw.indexOf(';');
        String value = separator >= 0 ? raw.substring(0, separator) : raw;
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
