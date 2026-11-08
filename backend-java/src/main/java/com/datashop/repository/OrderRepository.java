package com.datashop.repository;

import com.datashop.domain.Order;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(EntityManager entityManager, Order order);

    Optional<Order> findByIdAndUserId(EntityManager entityManager, UUID orderId, UUID userId);

    List<Order> findByUserId(EntityManager entityManager, UUID userId);

    boolean hasPaidProduct(EntityManager entityManager, UUID userId, UUID productId);
}
