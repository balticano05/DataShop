package com.datashop.repository;

import com.datashop.domain.Cart;
import com.datashop.domain.CartItem;
import com.datashop.domain.User;
import jakarta.persistence.EntityManager;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository {

    Optional<Cart> findByUserId(EntityManager entityManager, UUID userId);

    Cart save(EntityManager entityManager, Cart cart);

    Cart createFor(EntityManager entityManager, User user);

    Optional<CartItem> findItem(EntityManager entityManager, UUID itemId);

    Optional<CartItem> findItem(EntityManager entityManager, UUID cartId, UUID productId);

    void deleteItem(EntityManager entityManager, CartItem item);
}
