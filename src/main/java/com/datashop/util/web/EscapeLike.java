package com.datashop.util.web;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class EscapeLike {

    public static String escape(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder result = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character == '\\' || character == '%' || character == '_') {
                result.append('\\');
            }
            result.append(character);
        }
        return result.toString();
    }
}
