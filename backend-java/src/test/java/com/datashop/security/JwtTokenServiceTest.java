package com.datashop.security;

import com.datashop.domain.User;
import com.datashop.domain.UserRole;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    private final JwtTokenService service = new JwtTokenService(
            "test-secret-that-is-long-enough-for-hmac-sha256", Duration.ofMinutes(5));

    @Test
    void issuesAndParsesToken() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setRole(UserRole.ADMIN);

        AuthUser parsed = service.parse(service.issue(user)).orElseThrow();

        assertThat(parsed.getId()).isEqualTo(user.getId());
        assertThat(parsed.getEmail()).isEqualTo("user@example.com");
        assertThat(parsed.isAdmin()).isTrue();
    }

    @Test
    void rejectsInvalidToken() {
        assertThat(service.parse("not-a-token")).isEmpty();
        assertThat(service.parse(null)).isEmpty();
    }

    @Test
    void rejectsExpiredToken() {
        JwtTokenService expired = new JwtTokenService(
                "test-secret-that-is-long-enough-for-hmac-sha256", Duration.ofSeconds(-1));
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setRole(UserRole.CUSTOMER);

        assertThat(expired.parse(expired.issue(user))).isEmpty();
    }
}
