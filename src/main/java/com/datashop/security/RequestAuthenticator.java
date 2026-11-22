package com.datashop.security;

import com.datashop.domain.user.AuthUser;
import io.javalin.http.Context;

import java.util.Optional;

public interface RequestAuthenticator {

    Optional<AuthUser> optional(Context context);

    AuthUser require(Context context);

    AuthUser requireAdmin(Context context);
}
