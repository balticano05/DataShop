package com.datashop.util.web;

import com.datashop.util.Constants;
import com.datashop.web.ApiException;
import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public final class Errors {

    public static ApiException badRequest(String message) {
        return new ApiException(Constants.Http.Status.BAD_REQUEST, message);
    }

    public static ApiException unauthorized(String message) {
        return new ApiException(
                Constants.Http.Status.UNAUTHORIZED,
                orDefault(message, Constants.Messages.AUTHENTICATION_REQUIRED));
    }

    public static ApiException forbidden(String message) {
        return new ApiException(
                Constants.Http.Status.FORBIDDEN,
                orDefault(message, Constants.Messages.INSUFFICIENT_PERMISSIONS));
    }

    public static ApiException notFound(String message) {
        return new ApiException(Constants.Http.Status.NOT_FOUND, message);
    }

    public static ApiException conflict(String message) {
        return new ApiException(Constants.Http.Status.CONFLICT, message);
    }

    public static ApiException tooManyRequests(String message) {
        return new ApiException(Constants.Http.Status.TOO_MANY_REQUESTS, message);
    }

    private static String orDefault(String message, String fallback) {
        return Optional.ofNullable(message)
                .filter(value -> !value.isBlank())
                .orElse(fallback);
    }
}
