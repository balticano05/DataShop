package com.datashop.web.dto.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public final class AuthResponse {

    private final String accessToken;
    private final UserView user;
}
