package com.datashop.service;

import java.time.Duration;
import java.util.Optional;

public interface CatalogCache extends AutoCloseable {

    <T> Optional<T> get(String key, Class<T> type);

    void put(String key, Object value, Duration ttl);

    void invalidateAll();

    @Override
    default void close() {
    }
}
