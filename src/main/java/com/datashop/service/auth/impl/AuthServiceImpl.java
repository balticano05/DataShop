package com.datashop.service.auth.impl;

import com.datashop.db.Jpa;
import com.datashop.domain.user.User;
import com.datashop.domain.user.UserRole;
import com.datashop.repository.CartRepository;
import com.datashop.repository.UserRepository;
import com.datashop.security.PasswordEncoder;
import com.datashop.service.auth.AuthService;
import com.datashop.util.Constants;
import com.datashop.util.validation.Validation;
import com.datashop.util.web.Errors;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final Jpa jpa;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(String rawEmail, String rawPassword) {
        String email = Validation.email(rawEmail);
        String password = Validation.password(rawPassword, true);
        String passwordHash = passwordEncoder.hash(password);

        return jpa.write(entityManager -> {
            if (userRepository.existsByEmail(entityManager, email)) {
                throw Errors.conflict(Constants.Messages.EMAIL_ALREADY_REGISTERED);
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

    @Override
    public User login(String rawEmail, String rawPassword) {
        String email = Validation.email(rawEmail);
        String password = Validation.password(rawPassword, false);

        return jpa.read(entityManager -> {
            User user = userRepository.findByEmail(entityManager, email)
                    .orElseThrow(() -> Errors.unauthorized(Constants.Messages.INVALID_EMAIL_OR_PASSWORD));
            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                throw Errors.unauthorized(Constants.Messages.INVALID_EMAIL_OR_PASSWORD);
            }
            return user;
        });
    }

    @Override
    public User require(UUID userId) {
        return jpa.read(entityManager -> userRepository.findById(entityManager, userId)
                .orElseThrow(() -> Errors.unauthorized(Constants.Messages.USER_NOT_FOUND)));
    }

    @Override
    public User requireAdmin(UUID userId) {
        User user = require(userId);
        if (user.getRole() != UserRole.ADMIN) {
            throw Errors.forbidden(Constants.Messages.INSUFFICIENT_PERMISSIONS);
        }
        return user;
    }
}
