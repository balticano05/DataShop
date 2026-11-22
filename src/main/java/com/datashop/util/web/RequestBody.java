package com.datashop.util.web;

import com.datashop.util.Constants;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public final class RequestBody {

    public static <T> T require(Context context, Class<T> type) {
        try {
            T body = context.bodyAsClass(type);
            return Optional.ofNullable(body)
                    .orElseThrow(() -> Errors.badRequest(Constants.Messages.REQUEST_BODY_REQUIRED));
        } catch (BadRequestResponse exception) {
            throw Errors.badRequest(Constants.Messages.MALFORMED_REQUEST_BODY);
        }
    }
}
