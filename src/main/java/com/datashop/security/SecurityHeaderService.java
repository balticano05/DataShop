package com.datashop.security;

import io.javalin.http.Context;

public interface SecurityHeaderService {

    void apply(Context context);
}
