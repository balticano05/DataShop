package com.datashop.web.dto.auth;

import com.datashop.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public final class UserView {

    private final String id;
    private final String email;
    private final String role;

    public static UserView from(User user) {
        return UserView.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .role(user.getRole().value())
                .build();
    }
}
