package com.datashop.web.api;

import com.datashop.db.Jpa;
import io.javalin.Javalin;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.Map;

public class HealthApiController {

    private final Jpa jpa;

    public HealthApiController(Jpa jpa) {
        this.jpa = jpa;
    }

    public void register(Javalin app) {
        app.get("/api/health", context -> context.json(Map.of(
                "status", "ok",
                "timestamp", Instant.now().toString(),
                "uptime", ManagementFactory.getRuntimeMXBean().getUptime() / 1000
        )));

        app.get("/api/health/ready", context -> {
            boolean ready = jpa.healthCheck();
            context.status(ready ? 200 : 503).json(Map.of(
                    "status", ready ? "ready" : "unavailable",
                    "database", ready ? "up" : "down",
                    "timestamp", Instant.now().toString()
            ));
        });
    }
}
