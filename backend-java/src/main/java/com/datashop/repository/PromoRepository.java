package com.datashop.repository;

import com.datashop.domain.PromoCode;
import jakarta.persistence.EntityManager;

import java.util.Optional;
import java.util.UUID;

public interface PromoRepository {

    Optional<PromoCode> findByCode(EntityManager entityManager, String code);

    Optional<PromoCode> findById(EntityManager entityManager, UUID id);

    PromoCode save(EntityManager entityManager, PromoCode promoCode);
}
