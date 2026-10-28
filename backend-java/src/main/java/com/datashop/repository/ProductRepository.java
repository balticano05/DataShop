package com.datashop.repository;

import com.datashop.domain.Product;
import com.datashop.query.ProductPage;
import com.datashop.query.ProductQuery;
import jakarta.persistence.EntityManager;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Optional<Product> findById(EntityManager entityManager, UUID id);

    Product save(EntityManager entityManager, Product product);

    void delete(EntityManager entityManager, Product product);

    boolean decrementStock(EntityManager entityManager, UUID productId, int quantity);

    ProductPage search(EntityManager entityManager, ProductQuery query);
}
