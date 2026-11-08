package com.datashop.util;

import com.datashop.web.ApiException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationTest {

    @Test
    void normalizesEmail() {
        assertThat(Validation.email("  User@Example.COM ")).isEqualTo("user@example.com");
    }

    @Test
    void rejectsInvalidEmail() {
        assertThatThrownBy(() -> Validation.email("not-an-email"))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(400);
    }

    @Test
    void enforcesRegistrationPasswordLength() {
        assertThatThrownBy(() -> Validation.password("short", true))
                .isInstanceOf(ApiException.class);
        assertThat(Validation.password("long-enough", true)).isEqualTo("long-enough");
    }

    @Test
    void requiresThreeValidImageUrls() {
        assertThat(Validation.imageUrls(List.of(
                "https://example.com/1.png",
                "http://example.com/2.png",
                "https://example.com/3.png"
        ))).hasSize(3);

        assertThatThrownBy(() -> Validation.imageUrls(List.of("https://example.com/1.png")))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> Validation.imageUrls(List.of(
                "https://example.com/1.png",
                "https://example.com/2.png",
                "ftp://example.com/3.png"
        ))).isInstanceOf(ApiException.class);
    }
}
