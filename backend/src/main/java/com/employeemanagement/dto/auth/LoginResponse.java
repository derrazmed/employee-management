package com.employeemanagement.dto.auth;

import com.employeemanagement.entity.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private Long userId;
    private String name;
    private String email;
    private UserType userType;
    private List<String> permissions;
}