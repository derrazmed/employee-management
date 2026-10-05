package com.employeemanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateAccessToken(String email) {
        return generateToken(
                email,
                accessExpiration,
                "ACCESS"
        );
    }

    public String generateRefreshToken(String email) {
        return generateToken(
                email,
                refreshExpiration,
                "REFRESH"
        );
    }

    private String generateToken(
            String email,
            long expiration,
            String tokenType
    ) {
        Date now = new Date();

        Date expirationDate =
                new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(email)
                .claim("type", tokenType)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    public String extractEmail(String token) {
        return getClaims(token)
                .getSubject();
    }

    public String extractTokenType(String token) {
        return getClaims(token)
                .get("type", String.class);
    }

    public boolean isTokenValid(
            String token,
            String expectedType
    ) {
        try {
            Claims claims = getClaims(token);

            String tokenType =
                    claims.get("type", String.class);

            return claims.getExpiration().after(new Date())
                    && expectedType.equals(tokenType);

        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}