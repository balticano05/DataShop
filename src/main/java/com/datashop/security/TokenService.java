package com.datashop.security;

import com.datashop.domain.user.AuthUser;
import com.datashop.domain.user.User;

import java.util.Optional;

public interface TokenService {

    String issue(User user);

    Optional<AuthUser> parse(String token);
}
