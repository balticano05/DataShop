package com.datashop.service.media.impl;

import com.datashop.config.AppConfig;
import com.datashop.db.Jpa;
import com.datashop.domain.media.MediaAsset;
import com.datashop.domain.product.Product;
import com.datashop.repository.MediaAssetRepository;
import com.datashop.service.catalog.ProductService;
import com.datashop.service.media.MediaImage;
import com.datashop.service.media.MediaService;
import com.datashop.service.media.MediaStorage;
import com.datashop.util.Constants;
import com.datashop.util.id.Uuids;
import com.datashop.util.web.Errors;
import com.datashop.web.dto.media.UploadedAssetView;
import io.javalin.http.UploadedFile;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaStorage mediaStorage;
    private final ProductService productService;
    private final AppConfig config;
    private final Jpa jpa;
    private final MediaAssetRepository mediaAssetRepository;

    @Override
    public UploadedAssetView uploadPreview(UploadedFile file) {
        if (file == null) {
            throw Errors.badRequest(Constants.Messages.FILE_REQUIRED);
        }
        if (file.size() > config.getPreviewMaxBytes()) {
            throw Errors.badRequest(Constants.Messages.PREVIEW_TOO_LARGE);
        }
        String contentType = normalizeContentType(file.contentType(), "");
        if (!Constants.Media.PREVIEW_TYPES.contains(contentType)) {
            throw Errors.badRequest(Constants.Messages.UNSUPPORTED_IMAGE_TYPE + contentType);
        }
        byte[] data;
        try {
            data = file.content().readAllBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read uploaded image", exception);
        }
        MediaAsset asset = new MediaAsset();
        asset.setContentType(contentType);
        asset.setData(data);
        MediaAsset saved = jpa.write(entityManager ->
                mediaAssetRepository.save(entityManager, asset));
        return UploadedAssetView.builder()
                .publicId(saved.getId().toString())
                .url(Constants.Media.ASSET_PATH_PREFIX + saved.getId())
                .build();
    }

    @Override
    public Optional<MediaImage> loadImage(UUID id) {
        return jpa.read(entityManager -> mediaAssetRepository.findById(entityManager, id))
                .map(asset -> new MediaImage(asset.getContentType(), asset.getData()));
    }

    @Override
    public UploadedAssetView uploadDataset(String rawProductId, UploadedFile file) {
        if (file == null) {
            throw Errors.badRequest(Constants.Messages.FILE_REQUIRED);
        }
        if (file.size() > config.getDatasetMaxBytes()) {
            throw Errors.badRequest(Constants.Messages.DATASET_TOO_LARGE);
        }
        UUID productId = Uuids.parse(rawProductId);
        Product product = productService.requireEntity(productId);

        String contentType = normalizeContentType(
                file.contentType(), Constants.Media.DEFAULT_CONTENT_TYPE);
        if (!Constants.Media.DATASET_TYPES.contains(contentType)) {
            throw Errors.badRequest(Constants.Messages.UNSUPPORTED_DATASET_TYPE + contentType);
        }
        MediaStorage.StoredMedia stored = mediaStorage.store(
                file.content(), file.filename(), contentType);
        productService.setFilePublicId(productId, stored.publicId());
        Optional.ofNullable(product.getFilePublicId())
                .filter(publicId -> !publicId.equals(stored.publicId()))
                .ifPresent(mediaStorage::delete);
        return UploadedAssetView.builder()
                .publicId(stored.publicId())
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
