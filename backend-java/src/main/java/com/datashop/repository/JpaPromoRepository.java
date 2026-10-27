package com.datashop.repository;

import com.datashop.domain.PromoCode;
import jakarta.persistence.EntityManager;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class JpaPromoRepository implements PromoRepository {

    @Override
    public Optional<PromoCode> findByCode(EntityManager entityManager, String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return entityManager.createQuery(
                        "select p from PromoCode p where upper(p.code) = :code", PromoCode.class)
                .setParameter("code", code.trim().toUpperCase(Locale.ROOT))
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<PromoCode> findById(EntityManager entityManager, UUID id) {
        return Optional.ofNullable(entityManager.find(PromoCode.class, id));
    }

    @Override
    public PromoCode save(EntityManager entityManager, PromoCode promoCode) {
        if (promoCode.getId() == null) {
            entityManager.persist(promoCode);
            return promoCode;
        }
        return entityManager.merge(promoCode);
    }
}
