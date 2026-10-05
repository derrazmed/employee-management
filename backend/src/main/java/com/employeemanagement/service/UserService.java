package com.employeemanagement.service;

import com.employeemanagement.dto.user.UserPermissionsRequest;
import com.employeemanagement.dto.user.UserRequest;
import com.employeemanagement.dto.user.UserResponse;
import com.employeemanagement.dto.user.UserStatusRequest;
import com.employeemanagement.entity.Permission;
import com.employeemanagement.entity.User;
import com.employeemanagement.entity.UserStatus;
import com.employeemanagement.entity.UserType;
import com.employeemanagement.exception.ConflictException;
import com.employeemanagement.exception.ForbiddenException;
import com.employeemanagement.exception.ResourceNotFoundException;
import com.employeemanagement.repository.UserRepository;
import com.employeemanagement.security.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionService permissionService;

    public Page<UserResponse> getUsers(
            String search,
            Pageable pageable
    ) {

        permissionService.requireSuperAdmin();

        Page<User> users;

        if (search == null || search.trim().isEmpty()) {

            users = userRepository.findAll(pageable);

        } else {

            users = userRepository
                    .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            search,
                            search,
                            pageable
                    );
        }

        return users.map(this::mapToResponse);
    }

    public UserResponse getUser(Long id) {

        permissionService.requireSuperAdmin();
        return mapToResponse(findUser(id));
    }

    public UserResponse createUser(UserRequest request) {

        permissionService.requireSuperAdmin();
        if (request.getPassword() == null ||
                request.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "Password is required when creating a user"
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {

            throw new ConflictException(
                    "Email is already registered"
            );
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setUserType(request.getUserType());
        user.setStatus(UserStatus.ACTIVE);

        user.setPermissions(new HashSet<>());

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    public UserResponse updateUser(
            Long id,
            UserRequest request
    ) {

        permissionService.requireSuperAdmin();
        User user = findUser(id);

        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {

            throw new ConflictException(
                    "Email is already registered"
            );
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setUserType(request.getUserType());

        if (request.getPassword() != null &&
                !request.getPassword().isBlank()) {

            user.setPassword(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        User updatedUser = userRepository.save(user);

        return mapToResponse(updatedUser);
    }

    public void deleteUser(Long id) {

        permissionService.requireSuperAdmin();
        User user = findUser(id);

        userRepository.delete(user);
    }

    public UserResponse updatePermissions(
            Long id,
            UserPermissionsRequest request
    ) {

        permissionService.requireSuperAdmin();
        User user = findUser(id);

        /*
         * Super Admins already have full access.
         * We keep their permissions complete as well.
         */
        if (user.getUserType() == UserType.SUPER_ADMIN) {

            user.setPermissions(
                    new HashSet<>(
                            java.util.Set.of(
                                    Permission.CREATE,
                                    Permission.READ,
                                    Permission.UPDATE,
                                    Permission.DELETE
                            )
                    )
            );

        } else {

            user.setPermissions(
                    new HashSet<>(request.getPermissions())
            );
        }

        User updatedUser = userRepository.save(user);

        return mapToResponse(updatedUser);
    }

    public UserResponse updateStatus(
            Long id,
            UserStatusRequest request
    ) {

        permissionService.requireSuperAdmin();
        User user = findUser(id);

        user.setStatus(request.getStatus());

        User updatedUser = userRepository.save(user);

        return mapToResponse(updatedUser);
    }

    private User findUser(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        )
                );
    }

    private UserResponse mapToResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getUserType(),
                user.getStatus(),
                new HashSet<>(user.getPermissions())
        );
    }
}