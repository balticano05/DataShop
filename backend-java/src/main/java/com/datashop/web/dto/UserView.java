package com.datashop.web.dto;

import com.datashop.domain.User;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserView {

    String id;
    String email;
    String role;

    public static UserView from(User user) {
        return UserView.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .role(user.getRole().value())
                .build();
    }
}
