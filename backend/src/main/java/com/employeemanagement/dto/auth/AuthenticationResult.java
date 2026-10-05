package com.employeemanagement.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticationResult {

    private final String accessToken;
    private final String refreshToken;
    private final LoginResponse loginResponse;
}