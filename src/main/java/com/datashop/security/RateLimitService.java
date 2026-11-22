package com.datashop.security;

import io.javalin.http.Context;

import java.time.Duration;

public interface RateLimitService {

    boolean allow(String key, int limit, Duration window);

    void enforce(Context context, String action, int limit, Duration window);
}
