package com.datashop.repository;

import com.datashop.domain.User;
import jakarta.persistence.EntityManager;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class JpaUserRepository implements UserRepository {

    @Override
    public Optional<User> findByEmail(EntityManager entityManager, String email) {
        return entityManager.createQuery("select u from User u where u.email = :email", User.class)
                .setParameter("email", normalize(email))
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<User> findById(EntityManager entityManager, UUID id) {
        return Optional.ofNullable(entityManager.find(User.class, id));
    }

    @Override
    public User save(EntityManager entityManager, User user) {
        user.setEmail(normalize(user.getEmail()));
        if (user.getId() == null) {
            entityManager.persist(user);
            return user;
        }
        return entityManager.merge(user);
    }

    @Override
    public boolean existsByEmail(EntityManager entityManager, String email) {
        Long count = entityManager.createQuery(
                        "select count(u) from User u where u.email = :email", Long.class)
                .setParameter("email", normalize(email))
                .getSingleResult();
        return count != null && count > 0;
    }

    private String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
