package com.datashop.repository;

import com.datashop.domain.Product;
import com.datashop.query.ProductPage;
import com.datashop.query.ProductQuery;
import com.datashop.query.ProductSortField;
import com.datashop.query.SortOrder;
import com.datashop.util.EscapeLike;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class JpaProductRepository implements ProductRepository {

    @Override
    public Optional<Product> findById(EntityManager entityManager, UUID id) {
        Product product = entityManager.find(Product.class, id);
        if (product == null || product.getDeletedAt() != null) {
            return Optional.empty();
        }
        return Optional.of(product);
    }

    @Override
    public Product save(EntityManager entityManager, Product product) {
        if (product.getId() == null) {
            entityManager.persist(product);
            entityManager.flush();
            return product;
        }
        Product merged = entityManager.merge(product);
        entityManager.flush();
        return merged;
    }

    @Override
    public void delete(EntityManager entityManager, Product product) {
        product.setDeletedAt(Instant.now());
        entityManager.createQuery("delete from CartItem ci where ci.product.id = :productId")
                .setParameter("productId", product.getId())
                .executeUpdate();
    }

    @Override
    public boolean decrementStock(EntityManager entityManager, UUID productId, int quantity) {
        int updated = entityManager.createQuery("""
                        update Product p
                        set p.stockQuantity = p.stockQuantity - :quantity,
                            p.updatedAt = :now
                        where p.id = :productId
                          and p.deletedAt is null
                          and p.stockQuantity >= :quantity
                        """)
                .setParameter("quantity", quantity)
                .setParameter("now", Instant.now())
                .setParameter("productId", productId)
                .executeUpdate();
        return updated > 0;
    }

    @Override
    public ProductPage search(EntityManager entityManager, ProductQuery query) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Product> select = builder.createQuery(Product.class);
        Root<Product> root = select.from(Product.class);
        select.where(predicates(builder, root, query));
        select.orderBy(order(builder, root, query));

        List<Product> items = entityManager.createQuery(select)
                .setFirstResult(query.offset())
                .setMaxResults(query.getLimit())
                .getResultList();

        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<Product> countRoot = countQuery.from(Product.class);
        countQuery.select(builder.count(countRoot));
        countQuery.where(predicates(builder, countRoot, query));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new ProductPage(items, total == null ? 0 : total, query.getPage(), query.getLimit());
    }

    private Predicate[] predicates(CriteriaBuilder builder, Root<Product> root, ProductQuery query) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.isNull(root.get("deletedAt")));

        if (query.getSearch() != null && !query.getSearch().isBlank()) {
            String pattern = "%" + EscapeLike.escape(query.getSearch().trim().toLowerCase(Locale.ROOT)) + "%";
            predicates.add(builder.or(
                    builder.like(builder.lower(root.get("title")), pattern, '\\'),
                    builder.like(builder.lower(root.get("description")), pattern, '\\')
            ));
        }
        if (query.getMinPrice() != null) {
            predicates.add(builder.greaterThanOrEqualTo(root.get("price"), query.getMinPrice()));
        }
        if (query.getMaxPrice() != null) {
            predicates.add(builder.lessThanOrEqualTo(root.get("price"), query.getMaxPrice()));
        }
        if (query.getMinRating() != null) {
            predicates.add(builder.greaterThanOrEqualTo(root.get("rating"), query.getMinRating()));
        }
        return predicates.toArray(Predicate[]::new);
    }

    private List<Order> order(CriteriaBuilder builder, Root<Product> root, ProductQuery query) {
        String property = query.getSortBy().entityProperty();
        Order primary = query.getOrder() == SortOrder.ASC
                ? builder.asc(root.get(property))
                : builder.desc(root.get(property));
        Order stable = builder.asc(root.get("id"));
        return List.of(primary, stable);
    }
}
