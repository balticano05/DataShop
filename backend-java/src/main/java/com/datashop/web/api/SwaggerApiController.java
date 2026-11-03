package com.datashop.web.api;

import io.javalin.Javalin;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public class SwaggerApiController {

    private static final String HTML = read("swagger/index.html");
    private static final String JSON = read("openapi.json");

    public void register(Javalin app) {
        app.get("/api/docs", context -> context
                .contentType("text/html; charset=utf-8")
                .result(HTML));
        app.get("/api/docs-json", context -> context
                .contentType("application/json; charset=utf-8")
                .result(JSON));
        app.get("/api/openapi.json", context -> context
                .contentType("application/json; charset=utf-8")
                .result(JSON));
    }

    private static String read(String resource) {
        try (InputStream stream = SwaggerApiController.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalStateException("Missing classpath resource: " + resource);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
