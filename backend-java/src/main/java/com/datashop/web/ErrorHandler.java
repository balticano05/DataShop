package com.datashop.web;

import com.datashop.web.dto.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class ErrorHandler {

    private ErrorHandler() {
    }

    public static void register(Javalin app) {
        app.exception(ApiException.class, (exception, context) ->
                handle(context, exception.getStatus(), exception.getMessage()));

        app.exception(HttpResponseException.class, (exception, context) -> {
            int status = exception.getStatus();
            String message = status == 404
                    ? "Route not found"
                    : status == 405
                    ? "Method not allowed"
                    : status == 413
                    ? "Payload too large"
                    : "Malformed request";
            handle(context, status, message);
        });

        app.exception(JsonProcessingException.class, (exception, context) ->
                handle(context, 400, "Malformed request body"));

        app.exception(Exception.class, (exception, context) -> {
            log.error("Unhandled error at {} {}", context.method(), context.path(), exception);
            handle(context, 500, "Internal server error");
        });

        app.error(404, context -> handle(context, 404, "Route not found"));
        app.error(405, context -> handle(context, 405, "Method not allowed"));
        app.error(413, context -> handle(context, 413, "Payload too large"));
        app.error(500, context -> handle(context, 500, "Internal server error"));
    }

    private static void handle(Context context, int status, String message) {
        if (context.result() != null && !context.result().isEmpty()) {
            return;
        }
        boolean api = context.path().startsWith("/api/") || context.path().startsWith("/downloads/");
        if (api) {
            context.status(status).json(ErrorResponse.builder()
                    .statusCode(status)
                    .error(reason(status))
                    .message(message)
                    .path(context.path())
                    .timestamp(Instant.now().toString())
                    .build());
            return;
        }

        Map<String, Object> model = new LinkedHashMap<>();
        model.put("status", status);
        model.put("title", title(status));
        model.put("message", message == null || message.isBlank() ? reason(status) : message);
        context.status(status);
        try {
            context.render("error", model);
        } catch (RuntimeException exception) {
            log.error("Cannot render error page", exception);
            context.contentType("text/plain; charset=utf-8");
            context.result(status + " " + title(status) + "\n" + model.get("message"));
        }
    }

    private static String title(int status) {
        return switch (status) {
            case 400 -> "Некорректный запрос";
            case 401 -> "Требуется вход";
            case 403 -> "Доступ запрещён";
            case 404 -> "Страница не найдена";
            case 405 -> "Метод не поддерживается";
            case 409 -> "Конфликт данных";
            case 413 -> "Файл слишком большой";
            case 429 -> "Слишком много запросов";
            default -> "Ошибка сервера";
        };
    }

    private static String reason(int status) {
        return switch (status) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 409 -> "Conflict";
            case 413 -> "Payload Too Large";
            case 429 -> "Too Many Requests";
            default -> "Internal Server Error";
        };
    }
}
