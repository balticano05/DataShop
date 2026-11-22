package com.datashop.repository;

import com.datashop.domain.product.Product;
import com.datashop.domain.product.ProductPage;
import com.datashop.domain.product.ProductQuery;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Optional<Product> findById(EntityManager entityManager, UUID id);

    List<Product> findWithMissingImages(
            EntityManager entityManager, String assetPattern, int limit);

    Product save(EntityManager entityManager, Product product);

    void delete(EntityManager entityManager, Product product);

    boolean decrementStock(EntityManager entityManager, UUID productId, int quantity);

    ProductPage search(EntityManager entityManager, ProductQuery query);
}
