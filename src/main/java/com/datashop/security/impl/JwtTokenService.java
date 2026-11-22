package com.datashop.security.impl;

import com.datashop.domain.user.AuthUser;
import com.datashop.domain.user.User;
import com.datashop.domain.user.UserRole;
import com.datashop.security.TokenService;
import com.datashop.util.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;

public class JwtTokenService implements TokenService {

    private final SecretKey key;
    private final Duration ttl;

    public JwtTokenService(String secret, Duration ttl) {
        this.key = Keys.hmacShaKeyFor(normalizeSecret(secret));
        this.ttl = ttl;
    }

    @Override
    public String issue(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim(Constants.Security.JWT_EMAIL_CLAIM, user.getEmail())
                .claim(Constants.Security.JWT_ROLE_CLAIM, user.getRole().value())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    @Override
    public Optional<AuthUser> parse(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            UUID id = UUID.fromString(claims.getSubject());
            String email = claims.get(Constants.Security.JWT_EMAIL_CLAIM, String.class);
            String role = claims.get(Constants.Security.JWT_ROLE_CLAIM, String.class);
            return Optional.of(new AuthUser(id, email, UserRole.from(role)));
        } catch (JwtException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static byte[] normalizeSecret(String secret) {
        byte[] raw = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (raw.length >= Constants.Security.JWT_MIN_SECRET_LENGTH) {
            return raw;
        }
        try {
            return MessageDigest.getInstance(Constants.Security.SHA_256).digest(raw);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(Constants.Security.SHA_256_UNAVAILABLE, exception);
        }
    }
}
