package com.datashop.security;

import com.datashop.domain.UserRole;
import lombok.Value;

import java.util.UUID;

@Value
public class AuthUser {

    UUID id;
    String email;
    UserRole role;

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}
