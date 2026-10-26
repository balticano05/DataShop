package com.datashop.repository;

import com.datashop.domain.User;
import jakarta.persistence.EntityManager;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findByEmail(EntityManager entityManager, String email);

    Optional<User> findById(EntityManager entityManager, UUID id);

    User save(EntityManager entityManager, User user);

    boolean existsByEmail(EntityManager entityManager, String email);
}
