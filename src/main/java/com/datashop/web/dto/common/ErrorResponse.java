package com.datashop.web.dto.common;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public final class ErrorResponse {

    private final int statusCode;
    private final String error;
    private final Object message;
    private final String path;
    private final String timestamp;
}
