package com.employeemanagement.dto.user;

import com.employeemanagement.entity.Permission;
import com.employeemanagement.entity.UserStatus;
import com.employeemanagement.entity.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private UserType userType;
    private UserStatus status;
    private Set<Permission> permissions;
}