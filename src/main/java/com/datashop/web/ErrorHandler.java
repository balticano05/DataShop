package com.datashop.web;

import com.datashop.util.Constants;
import com.datashop.web.dto.common.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@UtilityClass
public class ErrorHandler {

    public static void register(Javalin app) {
        app.exception(ApiException.class, (exception, context) ->
                handle(context, exception.getStatus(), exception.getMessage()));

        app.exception(HttpResponseException.class, (exception, context) -> {
            int status = exception.getStatus();
            String message = switch (status) {
                case Constants.Http.Status.NOT_FOUND -> Constants.Messages.ROUTE_NOT_FOUND;
                case Constants.Http.Status.METHOD_NOT_ALLOWED -> Constants.Messages.METHOD_NOT_ALLOWED;
                case Constants.Http.Status.PAYLOAD_TOO_LARGE -> Constants.Messages.PAYLOAD_TOO_LARGE;
                default -> Constants.Messages.MALFORMED_REQUEST;
            };
            handle(context, status, message);
        });

        app.exception(JsonProcessingException.class, (exception, context) ->
                handle(context, Constants.Http.Status.BAD_REQUEST, Constants.Messages.MALFORMED_REQUEST_BODY));

        app.exception(Exception.class, (exception, context) -> {
            log.error("Unhandled error at {} {}", context.method(), context.path(), exception);
            handle(context, Constants.Http.Status.INTERNAL_SERVER_ERROR,
                    Constants.Messages.INTERNAL_SERVER_ERROR);
        });

        app.error(Constants.Http.Status.NOT_FOUND, context ->
                handle(context, Constants.Http.Status.NOT_FOUND, Constants.Messages.ROUTE_NOT_FOUND));
        app.error(Constants.Http.Status.METHOD_NOT_ALLOWED, context ->
                handle(context, Constants.Http.Status.METHOD_NOT_ALLOWED,
                        Constants.Messages.METHOD_NOT_ALLOWED));
        app.error(Constants.Http.Status.PAYLOAD_TOO_LARGE, context ->
                handle(context, Constants.Http.Status.PAYLOAD_TOO_LARGE,
                        Constants.Messages.PAYLOAD_TOO_LARGE));
        app.error(Constants.Http.Status.INTERNAL_SERVER_ERROR, context ->
                handle(context, Constants.Http.Status.INTERNAL_SERVER_ERROR,
                        Constants.Messages.INTERNAL_SERVER_ERROR));
    }

    private static void handle(Context context, int status, String message) {
        if (context.result() != null && !context.result().isEmpty()) {
            return;
        }
        boolean api = context.path().startsWith(Constants.Http.API_PREFIX)
                || context.path().startsWith(Constants.Http.DOWNLOADS_PREFIX);
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
        model.put(Constants.Attributes.STATUS, status);
        model.put(Constants.Attributes.TITLE, title(status));
        model.put(
                Constants.Attributes.MESSAGE,
                Optional.ofNullable(message)
                        .filter(value -> !value.isBlank())
                        .orElse(reason(status)));
        context.status(status);
        try {
            context.render(Constants.Templates.ERROR, model);
        } catch (RuntimeException exception) {
            log.error("Cannot render error page", exception);
            context.contentType(Constants.Http.CONTENT_TYPE_TEXT_UTF8);
            context.result(status + " " + title(status) + "\n" + model.get(Constants.Attributes.MESSAGE));
        }
    }

    private static String title(int status) {
        return switch (status) {
            case Constants.Http.Status.BAD_REQUEST -> Constants.Messages.TITLE_BAD_REQUEST;
            case Constants.Http.Status.UNAUTHORIZED -> Constants.Messages.TITLE_UNAUTHORIZED;
            case Constants.Http.Status.FORBIDDEN -> Constants.Messages.TITLE_FORBIDDEN;
            case Constants.Http.Status.NOT_FOUND -> Constants.Messages.TITLE_NOT_FOUND;
            case Constants.Http.Status.METHOD_NOT_ALLOWED -> Constants.Messages.TITLE_METHOD_NOT_ALLOWED;
            case Constants.Http.Status.CONFLICT -> Constants.Messages.TITLE_CONFLICT;
            case Constants.Http.Status.PAYLOAD_TOO_LARGE -> Constants.Messages.TITLE_PAYLOAD_TOO_LARGE;
            case Constants.Http.Status.TOO_MANY_REQUESTS -> Constants.Messages.TITLE_TOO_MANY_REQUESTS;
            default -> Constants.Messages.TITLE_INTERNAL_SERVER_ERROR;
        };
    }

    private static String reason(int status) {
        return switch (status) {
            case Constants.Http.Status.BAD_REQUEST -> Constants.Messages.REASON_BAD_REQUEST;
            case Constants.Http.Status.UNAUTHORIZED -> Constants.Messages.REASON_UNAUTHORIZED;
            case Constants.Http.Status.FORBIDDEN -> Constants.Messages.REASON_FORBIDDEN;
            case Constants.Http.Status.NOT_FOUND -> Constants.Messages.REASON_NOT_FOUND;
            case Constants.Http.Status.METHOD_NOT_ALLOWED -> Constants.Messages.REASON_METHOD_NOT_ALLOWED;
            case Constants.Http.Status.CONFLICT -> Constants.Messages.REASON_CONFLICT;
            case Constants.Http.Status.PAYLOAD_TOO_LARGE -> Constants.Messages.REASON_PAYLOAD_TOO_LARGE;
            case Constants.Http.Status.TOO_MANY_REQUESTS -> Constants.Messages.REASON_TOO_MANY_REQUESTS;
            default -> Constants.Messages.REASON_INTERNAL_SERVER_ERROR;
        };
    }
}
