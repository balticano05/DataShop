package com.datashop.domain.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString
@AllArgsConstructor
public final class AuthUser {

    private final UUID id;
    private final String email;
    private final UserRole role;

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}
