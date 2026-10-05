package com.employeemanagement.config;

import com.employeemanagement.security.JwtService;
import com.employeemanagement.entity.User;
import com.employeemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
public class WebSocketAuthInterceptor
        implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {

        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }

        HttpServletRequest httpRequest =
                servletRequest.getServletRequest();

        String accessToken =
                getAccessTokenFromCookie(httpRequest);

        if (accessToken == null) {
            return false;
        }

        if (!jwtService.isTokenValid(
                accessToken,
                "ACCESS"
        )) {
            return false;
        }

        String email =
                jwtService.extractEmail(accessToken);

        User user = userRepository
                .findByEmail(email)
                .orElse(null);

        if (user == null) {
            return false;
        }

        if (user.getStatus().name().equals("INACTIVE")) {
            return false;
        }

        attributes.put("email", user.getEmail());
        attributes.put("userType", user.getUserType());

        return true;
    }

    private String getAccessTokenFromCookie(
            HttpServletRequest request
    ) {

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if ("accessToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
    }
}