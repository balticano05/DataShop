package com.datashop.web;

import java.util.List;

public final class Errors {

    private Errors() {
    }

    public static ApiException badRequest(String message) {
        return new ApiException(400, message);
    }

    public static ApiException badRequest(List<String> messages) {
        return new ApiException(400, String.join(", ", messages));
    }

    public static ApiException unauthorized(String message) {
        return new ApiException(401, message == null || message.isBlank() ? "Authentication required" : message);
    }

    public static ApiException forbidden(String message) {
        return new ApiException(403, message == null || message.isBlank() ? "Insufficient permissions" : message);
    }

    public static ApiException notFound(String message) {
        return new ApiException(404, message);
    }

    public static ApiException conflict(String message) {
        return new ApiException(409, message);
    }

    public static ApiException tooManyRequests(String message) {
        return new ApiException(429, message);
    }
}
