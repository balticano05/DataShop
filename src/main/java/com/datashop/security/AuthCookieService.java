package com.datashop.security;

import io.javalin.http.Context;

public interface AuthCookieService {

    void write(Context context, String token);

    void clear(Context context);
}
