package com.datashop.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCatalogCache implements CatalogCache {

    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        Entry entry = entries.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.expiresAt.isBefore(Instant.now())) {
            entries.remove(key);
            return Optional.empty();
        }
        if (!type.isInstance(entry.value)) {
            return Optional.empty();
        }
        return Optional.of((T) entry.value);
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        entries.put(key, new Entry(value, Instant.now().plus(ttl)));
    }

    @Override
    public void invalidateAll() {
        entries.clear();
    }

    private record Entry(Object value, Instant expiresAt) {
    }
}
