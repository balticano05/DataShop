package com.datashop.repository;

import com.datashop.domain.Order;
import com.datashop.domain.OrderStatus;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaOrderRepository implements OrderRepository {

    @Override
    public Order save(EntityManager entityManager, Order order) {
        if (order.getId() == null) {
            entityManager.persist(order);
            entityManager.flush();
            return order;
        }
        Order merged = entityManager.merge(order);
        entityManager.flush();
        return merged;
    }

    @Override
    public Optional<Order> findByIdAndUserId(EntityManager entityManager, UUID orderId, UUID userId) {
        return entityManager.createQuery("""
                        select distinct o from Order o
                        left join fetch o.items
                        where o.id = :orderId and o.user.id = :userId
                        """, Order.class)
                .setParameter("orderId", orderId)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<Order> findByUserId(EntityManager entityManager, UUID userId) {
        return entityManager.createQuery("""
                        select distinct o from Order o
                        left join fetch o.items
                        where o.user.id = :userId
                        order by o.createdAt desc
                        """, Order.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public boolean hasPaidProduct(EntityManager entityManager, UUID userId, UUID productId) {
        Long count = entityManager.createQuery("""
                        select count(i) from OrderItem i
                        where i.order.user.id = :userId
                          and i.order.status = :status
                          and i.product.id = :productId
                        """, Long.class)
                .setParameter("userId", userId)
                .setParameter("status", OrderStatus.PAID)
                .setParameter("productId", productId)
                .getSingleResult();
        return count != null && count > 0;
    }
}
