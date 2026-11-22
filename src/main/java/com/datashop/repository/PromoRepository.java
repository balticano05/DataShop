package com.datashop.repository;

import com.datashop.domain.promo.PromoCode;
import jakarta.persistence.EntityManager;

import java.util.Optional;

public interface PromoRepository {

    Optional<PromoCode> findByCode(EntityManager entityManager, String code);
}
