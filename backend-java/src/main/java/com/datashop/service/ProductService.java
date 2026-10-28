package com.datashop.service;

import com.datashop.config.AppConfig;
import com.datashop.db.Jpa;
import com.datashop.domain.Product;
import com.datashop.query.ProductQuery;
import com.datashop.repository.ProductRepository;
import com.datashop.util.Uuids;
import com.datashop.util.Validation;
import com.datashop.web.Errors;
import com.datashop.web.dto.CreateProductRequest;
import com.datashop.web.dto.ProductView;
import com.datashop.web.dto.ProductsResponse;
import com.datashop.web.dto.UpdateProductRequest;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

@RequiredArgsConstructor
public class ProductService {

    private static final BigDecimal MAX_PRICE = new BigDecimal("99999999.99");
    private static final BigDecimal MAX_RATING = new BigDecimal("5.0");
    private static final int MAX_STOCK = Integer.MAX_VALUE;

    private final Jpa jpa;
    private final ProductRepository productRepository;
    private final CatalogCache catalogCache;
    private final AppConfig config;

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

    public ProductView get(String rawId) {
        return get(Uuids.parse(rawId));
    }

    public ProductView get(UUID id) {
        return jpa.read(entityManager -> ProductView.from(require(entityManager, id)));
    }

    public ProductView create(CreateProductRequest request) {
        String title = Validation.text(request.title(), "title", 3, 255);
        String description = Validation.textMin(request.description(), "description", 1);
        BigDecimal price = Validation.decimal(request.price(), "price", new BigDecimal("0.01"), MAX_PRICE);
        BigDecimal rating = request.rating() == null
                ? BigDecimal.ZERO
                : Validation.decimal(request.rating(), "rating", BigDecimal.ZERO, MAX_RATING);
        int stock = Validation.integer(request.stockQuantity(), "stockQuantity", 0, MAX_STOCK);

        Product product = new Product();
        product.setTitle(title);
        product.setDescription(description);
        product.setPrice(price);
        product.setRating(rating);
        product.setStockQuantity(stock);
        product.setImages(Validation.imageUrls(request.images()));
        product.setFilePublicId(Validation.publicId(request.filePublicId()));

        ProductView view = jpa.write(entityManager -> {
            Product saved = productRepository.save(entityManager, product);
            return ProductView.from(saved);
        });
        catalogCache.invalidateAll();
        return view;
    }

    public ProductView update(String rawId, UpdateProductRequest request) {
        UUID id = Uuids.parse(rawId);
        ProductView view = jpa.write(entityManager -> {
            Product product = require(entityManager, id);

            if (request.title() != null) {
                product.setTitle(Validation.text(request.title(), "title", 3, 255));
            }
            if (request.description() != null) {
                product.setDescription(Validation.textMin(request.description(), "description", 1));
            }
            if (request.price() != null) {
                product.setPrice(Validation.decimal(
                        request.price(), "price", new BigDecimal("0.01"), MAX_PRICE));
            }
            if (request.rating() != null) {
                product.setRating(Validation.decimal(
                        request.rating(), "rating", BigDecimal.ZERO, MAX_RATING));
            }
            if (request.images() != null) {
                product.setImages(Validation.imageUrls(request.images()));
            }
            if (request.stockQuantity() != null) {
                product.setStockQuantity(Validation.integer(
                        request.stockQuantity(), "stockQuantity", 0, MAX_STOCK));
            }
            if (request.filePublicId() != null) {
                product.setFilePublicId(Validation.publicId(request.filePublicId()));
            }

            return ProductView.from(productRepository.save(entityManager, product));
        });
        catalogCache.invalidateAll();
        return view;
    }

    public void delete(String rawId) {
        UUID id = Uuids.parse(rawId);
        jpa.write(entityManager -> {
            Product product = require(entityManager, id);
            productRepository.delete(entityManager, product);
            return null;
        });
        catalogCache.invalidateAll();
    }

    public void setFilePublicId(UUID productId, String filePublicId) {
        jpa.write(entityManager -> {
            Product product = require(entityManager, productId);
            product.setFilePublicId(filePublicId);
            return null;
        });
        catalogCache.invalidateAll();
    }

    public Product requireEntity(java.util.UUID id) {
        return jpa.read(entityManager -> require(entityManager, id));
    }

    private Product require(jakarta.persistence.EntityManager entityManager, UUID id) {
        return productRepository.findById(entityManager, id)
                .orElseThrow(() -> Errors.notFound("Product not found"));
    }

    public record CatalogResult(ProductsResponse response, boolean cacheHit) {
    }
}
