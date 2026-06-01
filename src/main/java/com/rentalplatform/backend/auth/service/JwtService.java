package com.rentalplatform.backend.auth.service;

import com.rentalplatform.backend.auth.security.CustomUserPrincipal;
import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import com.rentalplatform.backend.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;


@Service
public class JwtService {

    private final SecretKey key;

    public JwtService(@Value("${jwt.secret}") String jwtSecretKey) {
        this.key = Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    @Value("${jwt.access-token-expiration}")
    private Duration accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    public String generateAccessToken(CustomUserPrincipal principal) {

        return Jwts.builder()
                   .subject(principal.getEmail())
                   .id(UUID.randomUUID()
                           .toString())
                   .claim("uid", principal.getId())
                   .claim("role", principal.getRole()
                                           .name())
                   .issuedAt(new Date())
                   .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration.toMillis())) //15min
                   .signWith(key)
                   .compact();
    }

    public String generateRefreshToken(CustomUserPrincipal principal) {
        return Jwts.builder()
                   .subject(principal.getEmail())
                   .id(UUID.randomUUID()
                           .toString())
                   .claim("uid", principal.getId())
                   .issuedAt(new Date())
                   .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration.toMillis())) //7 days
                   .signWith(key)
                   .compact();
    }

    public UUID extractUserId(String token) {
        String userId = extractClaim(
                token,
                claims -> claims.get("uid", String.class)
        );

        return UUID.fromString(userId);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                       .verifyWith(key)
                       .build()
                       .parseSignedClaims(token)
                       .getPayload();
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UUID extractJti(String token) {
        String jti = extractClaim(token, Claims::getId);

        if (jti == null || jti.isBlank()) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        return UUID.fromString(jti);
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractClaim(token, Claims::getExpiration);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true; // error token = expired
        }
    }

    public boolean isTokenValid(String token, User user) {
        try {
            final String email = extractEmail(token);
            return email.equals(user.getEmail()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
