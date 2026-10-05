package com.employeemanagement.security;

import com.employeemanagement.entity.Permission;
import com.employeemanagement.entity.User;
import com.employeemanagement.entity.UserType;
import com.employeemanagement.exception.ForbiddenException;
import com.employeemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final UserRepository userRepository;

    public void requirePermission(Permission permission) {

        User user = getAuthenticatedUser();

        if (user.getUserType() == UserType.SUPER_ADMIN) {
            return;
        }

        if (!user.getPermissions().contains(permission)) {

            throw new ForbiddenException(
                    "You do not have permission to perform this action"
            );
        }
    }

    public void requireSuperAdmin() {

        User user = getAuthenticatedUser();

        if (user.getUserType() != UserType.SUPER_ADMIN) {

            throw new ForbiddenException(
                    "Only Super Administrators can manage users"
            );
        }
    }

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new ForbiddenException(
                    "Authentication is required"
            );
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ForbiddenException(
                                "Authenticated user not found"
                        )
                );
    }
}