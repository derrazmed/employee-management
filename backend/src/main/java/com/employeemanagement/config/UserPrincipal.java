package com.employeemanagement.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.Principal;

@Getter
@AllArgsConstructor
public class UserPrincipal implements Principal {

    private final String name;

    @Override
    public String getName() {
        return name;
    }
}