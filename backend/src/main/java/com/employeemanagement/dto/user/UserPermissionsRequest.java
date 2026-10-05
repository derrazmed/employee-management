package com.employeemanagement.dto.user;

import com.employeemanagement.entity.Permission;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionsRequest {

    @NotNull(message = "Permissions are required")
    private Set<Permission> permissions;
}