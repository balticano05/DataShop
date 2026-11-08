package com.datashop.repository;

import com.datashop.domain.Cart;
import com.datashop.domain.CartItem;
import com.datashop.domain.User;
import jakarta.persistence.EntityManager;

import java.util.Optional;
import java.util.UUID;

public class JpaCartRepository implements CartRepository {

    @Override
    public Optional<Cart> findByUserId(EntityManager entityManager, UUID userId) {
        return entityManager.createQuery("""
                        select distinct c from Cart c
                        left join fetch c.items i
                        left join fetch i.product
                        left join fetch c.promoCode
                        where c.user.id = :userId
                        """, Cart.class)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Cart save(EntityManager entityManager, Cart cart) {
        if (cart.getId() == null) {
            entityManager.persist(cart);
            return cart;
        }
        return entityManager.merge(cart);
    }

    @Override
    public Cart createFor(EntityManager entityManager, User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        entityManager.persist(cart);
        return cart;
    }

    @Override
    public Optional<CartItem> findItem(EntityManager entityManager, UUID itemId) {
        return entityManager.createQuery("""
                        select i from CartItem i
                        join fetch i.cart
                        join fetch i.product
                        where i.id = :itemId
                        """, CartItem.class)
                .setParameter("itemId", itemId)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<CartItem> findItem(EntityManager entityManager, UUID cartId, UUID productId) {
        return entityManager.createQuery("""
                        select i from CartItem i
                        join fetch i.product
                        where i.cart.id = :cartId and i.product.id = :productId
                        """, CartItem.class)
                .setParameter("cartId", cartId)
                .setParameter("productId", productId)
                .getResultStream()
                .findFirst();
    }

    @Override
    public void deleteItem(EntityManager entityManager, CartItem item) {
        entityManager.remove(item);
    }
}
