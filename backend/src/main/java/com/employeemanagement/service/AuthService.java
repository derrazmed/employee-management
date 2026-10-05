package com.employeemanagement.service;

import com.employeemanagement.dto.auth.AuthenticationResult;
import com.employeemanagement.dto.auth.LoginRequest;
import com.employeemanagement.dto.auth.LoginResponse;
import com.employeemanagement.dto.auth.RegisterRequest;
import com.employeemanagement.entity.Permission;
import com.employeemanagement.entity.User;
import com.employeemanagement.entity.UserStatus;
import com.employeemanagement.entity.UserType;
import com.employeemanagement.exception.ConflictException;
import com.employeemanagement.exception.ForbiddenException;
import com.employeemanagement.exception.UnauthorizedException;
import com.employeemanagement.repository.UserRepository;
import com.employeemanagement.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationResult login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new UnauthorizedException("Invalid email or password")
                );

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new ForbiddenException("Your account is inactive. Please contact an administrator.");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String accessToken =
                jwtService.generateAccessToken(user.getEmail());

        String refreshToken =
                jwtService.generateRefreshToken(user.getEmail());

        LoginResponse response = new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getUserType(),
                user.getPermissions().stream()
                    .map(Permission::name)
                    .toList()
        );

        return new AuthenticationResult(
                accessToken,
                refreshToken,
                response);
    }

    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already registered");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setUserType(UserType.NORMAL_USER);
        user.setStatus(UserStatus.ACTIVE);

        user.setPermissions(new HashSet<>());

        userRepository.save(user);
    }

    public AuthenticationResult refreshAccessToken(String refreshToken) {

        if (refreshToken == null || !jwtService.isTokenValid(refreshToken, "REFRESH")) {
            throw new UnauthorizedException(
                    "Invalid or expired refresh token"
            );
        }

        String email = jwtService.extractEmail(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UnauthorizedException(
                                "Invalid or expired refresh token"
                        )
                );

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new ForbiddenException(
                    "Your account is inactive. Please contact an administrator."
            );
        }

        String accessToken =
                jwtService.generateAccessToken(user.getEmail());

        LoginResponse loginResponse = new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getUserType(),
                user.getPermissions()
                        .stream()
                        .map(Permission::name)
                        .toList()
        );

        return new AuthenticationResult(
                accessToken,
                refreshToken,
                loginResponse
        );
    }
}