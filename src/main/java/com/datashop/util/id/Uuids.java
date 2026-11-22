package com.datashop.util.id;

import com.datashop.util.Constants;
import com.datashop.util.web.Errors;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public final class Uuids {

    public static UUID parse(String value) {
        if (value == null || value.isBlank()) {
            throw Errors.badRequest(Constants.Messages.IDENTIFIER_REQUIRED);
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException exception) {
            throw Errors.badRequest(Constants.Messages.INVALID_IDENTIFIER + value);
        }
    }
}
