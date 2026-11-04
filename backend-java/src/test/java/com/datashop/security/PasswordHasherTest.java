package com.datashop.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordHasherTest {

    private final PasswordHasher hasher = new PasswordHasher(1, 1024, 1);

    @Test
    void hashesAndMatchesPassword() {
        String hash = hasher.hash("S3cret-passphrase!");

        assertThat(hash).isNotEqualTo("S3cret-passphrase!");
        assertThat(hasher.matches("S3cret-passphrase!", hash)).isTrue();
        assertThat(hasher.matches("wrong", hash)).isFalse();
    }

    @Test
    void rejectsInvalidHash() {
        assertThat(hasher.matches("password", "not-a-hash")).isFalse();
        assertThat(hasher.matches("password", null)).isFalse();
    }
}
