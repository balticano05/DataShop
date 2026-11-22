package com.datashop.security.impl;

import com.datashop.security.RateLimitService;
import com.datashop.util.Constants;
import com.datashop.util.web.Errors;
import io.javalin.http.Context;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class RateLimiter implements RateLimitService {

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final AtomicLong calls = new AtomicLong();

    @Override
    public boolean allow(String key, int limit, Duration window) {
        long now = System.currentTimeMillis();
        long windowMillis = window.toMillis();
        Window bucket = windows.computeIfAbsent(key, ignored -> new Window());
        boolean allowed = bucket.allow(now, windowMillis, limit);
        if (calls.incrementAndGet() % Constants.Security.RATE_LIMIT_CLEANUP_INTERVAL == 0) {
            cleanup(now, windowMillis);
        }
        return allowed;
    }

    @Override
    public void enforce(Context context, String action, int limit, Duration window) {
        String key = action + ":" + context.ip();
        if (!allow(key, limit, window)) {
            throw Errors.tooManyRequests(Constants.Messages.TOO_MANY_REQUESTS);
        }
    }

    private void cleanup(long now, long windowMillis) {
        windows.entrySet().removeIf(entry -> entry.getValue().isStale(now, windowMillis));
    }

    private static final class Window {

        private final Deque<Long> timestamps = new ArrayDeque<>();

        synchronized boolean allow(long now, long windowMillis, int limit) {
            long threshold = now - windowMillis;
            while (!timestamps.isEmpty() && timestamps.peekFirst() < threshold) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= limit) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }

        synchronized boolean isStale(long now, long windowMillis) {
            Long last = timestamps.peekLast();
            return last == null || last < now - windowMillis;
        }
    }
}
