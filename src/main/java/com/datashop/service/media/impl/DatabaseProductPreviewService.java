package com.datashop.service.media.impl;

import com.datashop.db.Jpa;
import com.datashop.domain.media.MediaAsset;
import com.datashop.domain.product.Product;
import com.datashop.repository.MediaAssetRepository;
import com.datashop.repository.ProductRepository;
import com.datashop.service.catalog.CatalogCache;
import com.datashop.service.media.ProductPreviewService;
import com.datashop.util.Constants;
import com.datashop.util.media.ProductPreviewSvg;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Generates deterministic SVG previews and stores their bytes in Supabase PostgreSQL.
 *
 * <p>Product rows keep only internal {@code /media/assets/{id}} URLs. The binary
 * image data lives in the {@code media_assets} table and is served by the
 * application from the database. Deleted products are cleared instead of
 * receiving new previews.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class DatabaseProductPreviewService implements ProductPreviewService {

    private final Jpa jpa;
    private final ProductRepository productRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final CatalogCache catalogCache;

    @Override
    public void ensurePreviews() {
        List<Product> products = jpa.read(entityManager ->
                productRepository.findWithMissingImages(
                        entityManager,
                        Constants.Media.ASSET_PATTERN,
                        Constants.Media.PREVIEW_BOOTSTRAP_LIMIT));
        if (products.isEmpty()) {
            return;
        }

        int generated = 0;
        int cleared = 0;
        for (Product product : products) {
            if (product.getDeletedAt() != null) {
                clearImages(product.getId());
                cleared++;
                continue;
            }
            generateForProduct(product.getId());
            generated++;
        }
        catalogCache.invalidateAll();
        log.info("Stored database previews for {} products, cleared {} deleted products",
                generated, cleared);
    }

    @Override
    public void generatePreviews(EntityManager entityManager, Product product) {
        List<String> urls = new ArrayList<>(Constants.Media.PREVIEWS_PER_PRODUCT);
        for (int index = 0; index < Constants.Media.PREVIEWS_PER_PRODUCT; index++) {
            MediaAsset asset = new MediaAsset();
            asset.setContentType(Constants.Media.SVG_CONTENT_TYPE);
            asset.setData(ProductPreviewSvg.render(product.getTitle(), product.getId(), index)
                    .getBytes(StandardCharsets.UTF_8));
            MediaAsset saved = mediaAssetRepository.save(entityManager, asset);
            urls.add(Constants.Media.ASSET_PATH_PREFIX + saved.getId());
        }
        product.setImages(urls);
    }

    private void generateForProduct(UUID productId) {
        jpa.write(entityManager -> {
            Product managed = entityManager.find(Product.class, productId);
            if (managed != null) {
                generatePreviews(entityManager, managed);
            }
            return null;
        });
    }

    private void clearImages(UUID productId) {
        jpa.write(entityManager -> {
            Product managed = entityManager.find(Product.class, productId);
            if (managed != null) {
                managed.setImages(new ArrayList<>());
            }
            return null;
        });
    }
}
