package com.employeemanagement.controller;

import com.employeemanagement.dto.ApiResponse;
import com.employeemanagement.dto.auth.*;
import com.employeemanagement.service.AuthService;
import com.employeemanagement.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseCookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {

        AuthenticationResult result =
                authService.login(request);

        ResponseCookie accessCookie = ResponseCookie
                .from("accessToken", result.getAccessToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(120)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", result.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/api/auth")
                .maxAge(604800)
                .sameSite("Lax")
                .build();

        response.addHeader(
                "Set-Cookie",
                accessCookie.toString()
        );

        response.addHeader(
                "Set-Cookie",
                refreshCookie.toString()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Login successful",
                        result.getLoginResponse()
                )
        );
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                HttpStatus.CREATED.value(),
                                "User registered successfully",
                                null
                        )
                );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String refreshToken = null;

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        AuthenticationResult result =
                authService.refreshAccessToken(refreshToken);

        ResponseCookie accessCookie = ResponseCookie
                .from("accessToken", result.getAccessToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(120)
                .sameSite("Lax")
                .build();

        response.addHeader(
                "Set-Cookie",
                accessCookie.toString()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Access token refreshed successfully",
                        result.getLoginResponse()
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletResponse response
    ) {

        ResponseCookie accessCookie = ResponseCookie
                .from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/api/auth")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(
                "Set-Cookie",
                accessCookie.toString()
        );

        response.addHeader(
                "Set-Cookie",
                refreshCookie.toString()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Logout successful",
                        null
                )
        );
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request
    ) {

        passwordResetService.requestPasswordReset(
                request.getEmail()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "If an account exists with this email, a password reset code has been sent.",
                        null
                )
        );
    }

    @PostMapping("/password-reset/verify")
    public ResponseEntity<ApiResponse<Void>> verifyPasswordResetCode(
            @Valid @RequestBody PasswordResetVerifyRequest request
    ) {

        passwordResetService.verifyCode(
                request.getEmail(),
                request.getCode()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Verification code is valid.",
                        null
                )
        );
    }

    @PostMapping("/password-reset/complete")
    public ResponseEntity<ApiResponse<Void>> completePasswordReset(
            @Valid @RequestBody PasswordResetCompleteRequest request
    ) {

        passwordResetService.resetPassword(
                request.getEmail(),
                request.getCode(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Password reset successfully.",
                        null
                )
        );
    }
}