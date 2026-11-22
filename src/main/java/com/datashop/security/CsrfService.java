package com.datashop.security;

import io.javalin.http.Context;

public interface CsrfService {

    String token(Context context);

    void verify(Context context);
}
