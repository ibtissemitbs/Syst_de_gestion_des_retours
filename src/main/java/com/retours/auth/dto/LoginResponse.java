package com.retours.auth.dto;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private AuthUserResponse user;
}
