package com.datashop.repository.impl;

import com.datashop.domain.promo.PromoCode;
import com.datashop.repository.PromoRepository;
import jakarta.persistence.EntityManager;

import java.util.Locale;
import java.util.Optional;

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
}
