package com.datashop.service;

import com.datashop.db.Jpa;
import com.datashop.domain.User;
import com.datashop.domain.UserRole;
import com.datashop.repository.CartRepository;
import com.datashop.repository.UserRepository;
import com.datashop.security.PasswordHasher;
import com.datashop.util.Validation;
import com.datashop.web.Errors;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class AuthService {

    private final Jpa jpa;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordHasher passwordHasher;

    public User register(String rawEmail, String rawPassword) {
        String email = Validation.email(rawEmail);
        String password = Validation.password(rawPassword, true);
        String passwordHash = passwordHasher.hash(password);

        return jpa.write(entityManager -> {
            if (userRepository.existsByEmail(entityManager, email)) {
                throw Errors.conflict("Email is already registered");
            }
            User user = new User();
            user.setEmail(email);
            user.setPasswordHash(passwordHash);
            user.setRole(UserRole.CUSTOMER);
            User saved = userRepository.save(entityManager, user);
            cartRepository.createFor(entityManager, saved);
            return saved;
        });
    }

    public User login(String rawEmail, String rawPassword) {
        String email = Validation.email(rawEmail);
        String password = Validation.password(rawPassword, false);

        return jpa.read(entityManager -> {
            User user = userRepository.findByEmail(entityManager, email)
                    .orElseThrow(() -> Errors.unauthorized("Invalid email or password"));
            if (!passwordHasher.matches(password, user.getPasswordHash())) {
                throw Errors.unauthorized("Invalid email or password");
            }
            return user;
        });
    }

    public User require(UUID userId) {
        return jpa.read(entityManager -> userRepository.findById(entityManager, userId)
                .orElseThrow(() -> Errors.unauthorized("User no longer exists")));
    }
}
