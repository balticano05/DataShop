package com.datashop.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ErrorResponse {

    int statusCode;
    String error;
    Object message;
    String path;
    String timestamp;
}
