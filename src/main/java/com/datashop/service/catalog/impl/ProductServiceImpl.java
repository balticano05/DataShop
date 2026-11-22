package com.datashop.service.catalog.impl;

import com.datashop.config.AppConfig;
import com.datashop.db.Jpa;
import com.datashop.domain.product.Product;
import com.datashop.domain.product.ProductQuery;
import com.datashop.repository.MediaAssetRepository;
import com.datashop.repository.ProductRepository;
import com.datashop.service.catalog.CatalogCache;
import com.datashop.service.catalog.ProductService;
import com.datashop.service.media.ProductPreviewService;
import com.datashop.util.Constants;
import com.datashop.util.cache.CatalogCacheKeys;
import com.datashop.util.id.Uuids;
import com.datashop.util.validation.Validation;
import com.datashop.util.web.Errors;
import com.datashop.web.dto.product.CreateProductRequest;
import com.datashop.web.dto.product.ProductView;
import com.datashop.web.dto.product.ProductsResponse;
import com.datashop.web.dto.product.UpdateProductRequest;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final Jpa jpa;
    private final ProductRepository productRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final ProductPreviewService productPreviewService;
    private final CatalogCache catalogCache;
    private final AppConfig config;

    @Override
    public CatalogResult list(ProductQuery query) {
        String key = CatalogCacheKeys.forQuery(query);
        Duration ttl = Duration.ofSeconds(config.getCacheTtlSeconds());
        return catalogCache.get(key, ProductsResponse.class)
                .map(response -> new CatalogResult(response, true))
                .orElseGet(() -> {
                    ProductsResponse response = jpa.read(entityManager ->
                            ProductsResponse.from(productRepository.search(entityManager, query)));
                    catalogCache.put(key, response, ttl);
                    return new CatalogResult(response, false);
                });
    }

    @Override
    public ProductView get(String rawId) {
        UUID id = Uuids.parse(rawId);
        return jpa.read(entityManager -> ProductView.from(require(entityManager, id)));
    }

    @Override
    public ProductView create(CreateProductRequest request) {
        String title = Validation.text(
                request.title(),
                Constants.Params.TITLE,
                Constants.Validation.TITLE_MIN_LENGTH,
                Constants.Validation.TITLE_MAX_LENGTH);
        String description = Validation.textMin(
                request.description(),
                Constants.Params.DESCRIPTION,
                Constants.Validation.DESCRIPTION_MIN_LENGTH);
        BigDecimal price = Validation.decimal(
                request.price(), Constants.Params.PRICE,
                Constants.Validation.MIN_PRICE, Constants.Validation.MAX_PRICE);
        BigDecimal rating = Optional.ofNullable(request.rating())
                .map(value -> Validation.decimal(
                        value, Constants.Params.RATING,
                        Constants.Validation.MIN_RATING, Constants.Validation.MAX_RATING))
                .orElse(BigDecimal.ZERO);
        int stock = Validation.integer(
                request.stockQuantity(), Constants.Params.STOCK_QUANTITY,
                Constants.Validation.MIN_STOCK, Constants.Validation.MAX_STOCK);

        Product product = new Product();
        product.setTitle(title);
        product.setDescription(description);
        product.setPrice(price);
        product.setRating(rating);
        product.setStockQuantity(stock);
        product.setFilePublicId(Validation.publicId(request.filePublicId()));

        ProductView view = jpa.write(entityManager -> {
            List<String> images = resolveImages(entityManager, request.images());
            product.setImages(images);
            Product saved = productRepository.save(entityManager, product);
            if (images.isEmpty()) {
                productPreviewService.generatePreviews(entityManager, saved);
            }
            return ProductView.from(saved);
        });
        catalogCache.invalidateAll();
        return view;
    }

    @Override
    public ProductView update(String rawId, UpdateProductRequest request) {
        UUID id = Uuids.parse(rawId);
        ProductView view = jpa.write(entityManager -> {
            Product product = require(entityManager, id);

            Optional.ofNullable(request.title()).ifPresent(value ->
                    product.setTitle(Validation.text(
                            value,
                            Constants.Params.TITLE,
                            Constants.Validation.TITLE_MIN_LENGTH,
                            Constants.Validation.TITLE_MAX_LENGTH)));
            Optional.ofNullable(request.description()).ifPresent(value ->
                    product.setDescription(Validation.textMin(
                            value,
                            Constants.Params.DESCRIPTION,
                            Constants.Validation.DESCRIPTION_MIN_LENGTH)));
            Optional.ofNullable(request.price()).ifPresent(value ->
                    product.setPrice(Validation.decimal(
                            value, Constants.Params.PRICE,
                            Constants.Validation.MIN_PRICE, Constants.Validation.MAX_PRICE)));
            Optional.ofNullable(request.rating()).ifPresent(value ->
                    product.setRating(Validation.decimal(
                            value, Constants.Params.RATING,
                            Constants.Validation.MIN_RATING, Constants.Validation.MAX_RATING)));
            if (request.images() != null) {
                if (request.images().isEmpty()) {
                    productPreviewService.generatePreviews(entityManager, product);
                } else {
                    product.setImages(resolveImages(entityManager, request.images()));
                }
            }
            Optional.ofNullable(request.stockQuantity()).ifPresent(value ->
                    product.setStockQuantity(Validation.integer(
                            value, Constants.Params.STOCK_QUANTITY,
                            Constants.Validation.MIN_STOCK, Constants.Validation.MAX_STOCK)));
            Optional.ofNullable(request.filePublicId()).ifPresent(value ->
                    product.setFilePublicId(Validation.publicId(value)));

            return ProductView.from(productRepository.save(entityManager, product));
        });
        catalogCache.invalidateAll();
        return view;
    }

    @Override
    public void delete(String rawId) {
        UUID id = Uuids.parse(rawId);
        jpa.write(entityManager -> {
            Product product = require(entityManager, id);
            product.setImages(List.of());
            productRepository.delete(entityManager, product);
            return null;
        });
        catalogCache.invalidateAll();
    }

    @Override
    public void setFilePublicId(UUID productId, String filePublicId) {
        jpa.write(entityManager -> {
            Product product = require(entityManager, productId);
            product.setFilePublicId(filePublicId);
            return null;
        });
        catalogCache.invalidateAll();
    }

    @Override
    public Product requireEntity(UUID id) {
        return jpa.read(entityManager -> require(entityManager, id));
    }

    private Product require(EntityManager entityManager, UUID id) {
        return productRepository.findById(entityManager, id)
                .orElseThrow(() -> Errors.notFound(Constants.Messages.PRODUCT_NOT_FOUND));
    }

    private List<String> resolveImages(EntityManager entityManager, List<String> values) {
        List<String> images = Validation.imageUrls(values);
        for (String image : images) {
            UUID assetId = UUID.fromString(
                    image.substring(Constants.Media.ASSET_PATH_PREFIX.length()));
            if (mediaAssetRepository.findById(entityManager, assetId).isEmpty()) {
                throw Errors.badRequest(Constants.Messages.IMAGES_NOT_FOUND);
            }
        }
        return images;
    }
}
