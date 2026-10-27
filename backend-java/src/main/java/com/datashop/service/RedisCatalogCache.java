package com.datashop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.JedisPooled;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

public class RedisCatalogCache implements CatalogCache {

    private final JedisPooled jedis;
    private final ObjectMapper objectMapper;

    public RedisCatalogCache(String redisUrl, ObjectMapper objectMapper) {
        this.jedis = new JedisPooled(URI.create(redisUrl));
        this.objectMapper = objectMapper;
        try {
            this.jedis.ping();
        } catch (RuntimeException exception) {
            this.jedis.close();
            throw exception;
        }
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            String json = jedis.get(key);
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, type));
        } catch (Exception exception) {
            throw new IllegalStateException("Redis cache read failed for " + key, exception);
        }
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            jedis.setex(key, Math.max(1, ttl.toSeconds()), json);
        } catch (Exception exception) {
            throw new IllegalStateException("Redis cache write failed for " + key, exception);
        }
    }

    @Override
    public void invalidateAll() {
        try {
            var keys = jedis.keys("catalog:*");
            if (keys != null && !keys.isEmpty()) {
                jedis.del(keys.toArray(String[]::new));
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Redis cache invalidation failed", exception);
        }
    }

    @Override
    public void close() {
        jedis.close();
    }
}
