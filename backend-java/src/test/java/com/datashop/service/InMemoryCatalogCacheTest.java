package com.datashop.service;

import com.datashop.web.dto.ProductsResponse;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryCatalogCacheTest {

    private final InMemoryCatalogCache cache = new InMemoryCatalogCache();

    @Test
    void storesAndReadsTypedValue() {
        ProductsResponse response = ProductsResponse.builder()
                .items(List.of())
                .meta(null)
                .build();

        cache.put("catalog:test", response, Duration.ofMinutes(1));

        assertThat(cache.get("catalog:test", ProductsResponse.class)).containsSame(response);
        assertThat(cache.get("catalog:test", String.class)).isEmpty();
    }

    @Test
    void expiresEntries() throws InterruptedException {
        cache.put("catalog:test", "value", Duration.ofMillis(10));

        Thread.sleep(30);

        assertThat(cache.get("catalog:test", String.class)).isEmpty();
    }

    @Test
    void invalidationClearsEntries() {
        cache.put("catalog:test", "value", Duration.ofMinutes(1));
        cache.invalidateAll();

        assertThat(cache.get("catalog:test", String.class)).isEmpty();
    }
}
