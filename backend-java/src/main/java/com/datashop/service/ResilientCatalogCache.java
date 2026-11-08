package com.datashop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ResilientCatalogCache implements CatalogCache {

    private final CatalogCache delegate;

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            return delegate.get(key, type);
        } catch (RuntimeException exception) {
            log.warn("Catalog cache read failed, serving from database: {}", exception.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        try {
            delegate.put(key, value, ttl);
        } catch (RuntimeException exception) {
            log.warn("Catalog cache write failed: {}", exception.getMessage());
        }
    }

    @Override
    public void invalidateAll() {
        try {
            delegate.invalidateAll();
        } catch (RuntimeException exception) {
            log.warn("Catalog cache invalidation failed: {}", exception.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            delegate.close();
        } catch (RuntimeException exception) {
            log.warn("Catalog cache close failed: {}", exception.getMessage());
        }
    }
}
