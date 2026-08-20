package com.rentalplatform.backend.auth.service;

import com.rentalplatform.backend.auth.security.CustomUserPrincipal;
import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.user.entity.User;
import com.rentalplatform.backend.user.constant.UserRole;
import com.rentalplatform.backend.user.constant.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {
    private JwtService jwtService;

    private User user;
    private CustomUserPrincipal principal;

    private static final String SECRET =
            "this-is-a-very-long-secret-key-for-jwt-testing-256-bit";

    @BeforeEach
    void setUp() {

        jwtService = new JwtService(SECRET);

        ReflectionTestUtils.setField(
                jwtService,
                "accessTokenExpiration",
                Duration.ofMinutes(15)
        );

        ReflectionTestUtils.setField(
                jwtService,
                "refreshTokenExpiration",
                Duration.ofDays(7)
        );

        user = User.builder()
                   .id(UUID.randomUUID())
                   .email("admin@gmail.com")
                   .password("password")
                   .fullName("Admin")
                   .role(UserRole.CUSTOMER)
                   .status(UserStatus.ACTIVE)
                   .createdAt(Instant.now())
                   .build();

        principal = new CustomUserPrincipal(user);
    }

    @Test
    @DisplayName("Should generate access token")
    void shouldGenerateAccessToken() {

        String token =
                jwtService.generateAccessToken(principal);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("Should generate refresh token")
    void shouldGenerateRefreshToken() {

        String token =
                jwtService.generateRefreshToken(principal);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("Should extract email from token")
    void shouldExtractEmail() {

        String token =
                jwtService.generateAccessToken(principal);

        String email =
                jwtService.extractEmail(token);

        assertEquals(
                user.getEmail(),
                email
        );
    }

    @Test
    @DisplayName("Should extract user id from token")
    void shouldExtractUserId() {

        String token =
                jwtService.generateAccessToken(principal);

        UUID userId =
                jwtService.extractUserId(token);

        assertEquals(
                user.getId(),
                userId
        );
    }

    @Test
    @DisplayName("Should extract jti from token")
    void shouldExtractJti() {

        String token =
                jwtService.generateAccessToken(principal);

        UUID jti =
                jwtService.extractJti(token);

        assertNotNull(jti);
    }

    @Test
    @DisplayName("Should extract role from token")
    void shouldExtractRole() {
        String token = jwtService.generateAccessToken(principal);
        UserRole role = jwtService.extractRole(token);
        assertEquals(UserRole.CUSTOMER, role);
    }

    @Test
    @DisplayName("Should return true when token is statelessly valid")
    void shouldReturnTrueWhenTokenStatelesslyValid() {
        String token = jwtService.generateAccessToken(principal);
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    @DisplayName("Should return true when token is valid")
    void shouldReturnTrueWhenTokenValid() {

        String token =
                jwtService.generateAccessToken(principal);

        boolean result =
                jwtService.isTokenValid(token, user);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when email mismatch")
    void shouldReturnFalseWhenEmailMismatch() {

        String token =
                jwtService.generateAccessToken(principal);

        User anotherUser = User.builder()
                               .email("other@gmail.com")
                               .build();

        boolean result =
                jwtService.isTokenValid(token, anotherUser);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should throw exception when token malformed")
    void shouldThrowExceptionWhenTokenMalformed() {

        assertThrows(
                AppException.class,
                () -> jwtService.extractEmail("invalid-token")
        );
    }

    @Test
    @DisplayName("Should return true when token expired")
    void shouldReturnTrueWhenTokenExpired() {

        JwtService shortLivedJwtService =
                new JwtService(SECRET);

        ReflectionTestUtils.setField(
                shortLivedJwtService,
                "accessTokenExpiration",
                Duration.ofMillis(1)
        );

        ReflectionTestUtils.setField(
                shortLivedJwtService,
                "refreshTokenExpiration",
                Duration.ofMillis(1)
        );

        String token =
                shortLivedJwtService.generateAccessToken(principal);

        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {
        }

        assertTrue(
                shortLivedJwtService.isTokenExpired(token)
        );
    }
}
