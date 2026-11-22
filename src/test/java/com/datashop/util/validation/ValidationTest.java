package com.datashop.util.validation;

import com.datashop.web.ApiException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationTest {

    private static final String ASSET_1 =
            "/media/assets/00000000-0000-0000-0000-000000000001";
    private static final String ASSET_2 =
            "/media/assets/00000000-0000-0000-0000-000000000002";
    private static final String ASSET_3 =
            "/media/assets/00000000-0000-0000-0000-000000000003";

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
    void acceptsThreeDatabaseAssetUrls() {
        assertThat(Validation.imageUrls(List.of(ASSET_1, ASSET_2, ASSET_3)))
                .containsExactly(ASSET_1, ASSET_2, ASSET_3);

        assertThat(Validation.imageUrls(List.of(
                "https://datashop.example" + ASSET_1,
                "http://datashop.example" + ASSET_2,
                ASSET_3
        ))).containsExactly(ASSET_1, ASSET_2, ASSET_3);
    }

    @Test
    void allowsEmptyImagesForAutomaticPreviews() {
        assertThat(Validation.imageUrls(null)).isEmpty();
        assertThat(Validation.imageUrls(List.of())).isEmpty();
    }

    @Test
    void rejectsExternalImageUrls() {
        assertThatThrownBy(() -> Validation.imageUrls(List.of(
                "https://example.com/1.png",
                "https://example.com/2.png",
                "https://example.com/3.png"
        ))).isInstanceOf(ApiException.class)
                .hasMessageContaining("DataShop media asset");
    }

    @Test
    void rejectsFewerThanThreeUniqueAssets() {
        assertThatThrownBy(() -> Validation.imageUrls(List.of(ASSET_1)))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> Validation.imageUrls(List.of(ASSET_1, ASSET_1, ASSET_2)))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void rejectsMalformedAssetUrls() {
        assertThatThrownBy(() -> Validation.imageUrls(List.of(
                "/media/assets/not-a-uuid",
                ASSET_2,
                ASSET_3
        ))).isInstanceOf(ApiException.class);
    }
}
