package com.rentalplatform.backend.auth.service;

import com.rentalplatform.backend.auth.entity.RefreshToken;
import com.rentalplatform.backend.auth.repository.RefreshTokenRepository;
import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private UUID userId;
    private UUID tokenId;
    private String rawToken;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        tokenId = UUID.randomUUID();
        rawToken = "refresh-token";
    }

    @Test
    @DisplayName("Should create refresh token")
    void shouldCreateRefreshToken() {

        // Arrange
        when(jwtService.extractJti(rawToken))
                .thenReturn(tokenId);

        // Act
        refreshTokenService.createRefreshToken(
                userId,
                rawToken,
                "Chrome",
                "127.0.0.1"
        );

        // Assert
        ArgumentCaptor<RefreshToken> captor =
                ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository)
                .save(captor.capture());

        RefreshToken saved = captor.getValue();

        assertEquals(userId, saved.getUserId());
        assertEquals(tokenId, saved.getTokenId());

        assertEquals("Chrome", saved.getDevice());
        assertEquals("127.0.0.1", saved.getIpAddress());

        assertFalse(saved.isRevoked());

        assertNotNull(saved.getExpiryDate());
        assertNotNull(saved.getHashedToken());
    }
    @Test
    @DisplayName("Should verify refresh token")
    void shouldVerifyRefreshToken() {

        String hashedToken = invokeHash(rawToken);

        RefreshToken token = createValidToken(hashedToken);

        when(jwtService.extractJti(rawToken))
                .thenReturn(tokenId);

        when(refreshTokenRepository.findByTokenId(tokenId))
                .thenReturn(Optional.of(token));

        RefreshToken result =
                refreshTokenService.verifyRefreshToken(rawToken);

        assertEquals(token, result);
    }

    @Test
    @DisplayName("Should throw when token revoked")
    void shouldThrowWhenTokenRevoked() {

        RefreshToken token =
                createValidToken(invokeHash(rawToken));

        token.setRevoked(true);

        when(jwtService.extractJti(rawToken))
                .thenReturn(tokenId);

        when(refreshTokenRepository.findByTokenId(tokenId))
                .thenReturn(Optional.of(token));

        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> refreshTokenService.verifyRefreshToken(rawToken)
                );

        assertEquals(
                ErrorCode.TOKEN_REVOKED,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw when token expired")
    void shouldThrowWhenTokenExpired() {

        RefreshToken token =
                createValidToken(invokeHash(rawToken));

        token.setExpiryDate(
                Instant.now().minusSeconds(60)
        );

        when(jwtService.extractJti(rawToken))
                .thenReturn(tokenId);

        when(refreshTokenRepository.findByTokenId(tokenId))
                .thenReturn(Optional.of(token));

        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> refreshTokenService.verifyRefreshToken(rawToken)
                );

        assertEquals(
                ErrorCode.TOKEN_EXPIRED,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw when token hash mismatch")
    void shouldThrowWhenTokenHashMismatch() {

        RefreshToken token =
                createValidToken("different-hash");

        when(jwtService.extractJti(rawToken))
                .thenReturn(tokenId);

        when(refreshTokenRepository.findByTokenId(tokenId))
                .thenReturn(Optional.of(token));

        AppException exception =
                assertThrows(
                        AppException.class,
                        () -> refreshTokenService.verifyRefreshToken(rawToken)
                );

        assertEquals(
                ErrorCode.INVALID_TOKEN,
                exception.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should rotate refresh token")
    void shouldRotateRefreshToken() {

        String oldToken = "old-token";
        String newToken = "new-token";

        UUID oldJti = UUID.randomUUID();
        UUID newJti = UUID.randomUUID();

        RefreshToken existing =
                createValidToken(invokeHash(oldToken));

        when(jwtService.extractJti(oldToken))
                .thenReturn(oldJti);

        when(jwtService.extractJti(newToken))
                .thenReturn(newJti);

        when(refreshTokenRepository.findByTokenId(oldJti))
                .thenReturn(Optional.of(existing));

        refreshTokenService.rotateRefreshToken(
                oldToken,
                newToken
        );

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(2))
                .save(captor.capture());

        RefreshToken newlySavedToken = captor.getAllValues().get(1);
        assertEquals(invokeHash(newToken), newlySavedToken.getHashedToken());

        assertTrue(existing.isRevoked());
    }

    @Test
    @DisplayName("Should verify rotated refresh token successfully")
    void shouldVerifyRotatedRefreshTokenSuccessfully() {

        String oldToken = "old-token";
        String newToken = "new-token";

        UUID oldJti = UUID.randomUUID();
        UUID newJti = UUID.randomUUID();

        RefreshToken existing = createValidToken(invokeHash(oldToken));

        when(jwtService.extractJti(oldToken)).thenReturn(oldJti);
        when(jwtService.extractJti(newToken)).thenReturn(newJti);

        when(refreshTokenRepository.findByTokenId(oldJti)).thenReturn(Optional.of(existing));

        // Act: Rotate token
        refreshTokenService.rotateRefreshToken(oldToken, newToken);

        // Capture the newly rotated token saved in repository
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(2)).save(captor.capture());
        RefreshToken newlySavedToken = captor.getAllValues().get(1);

        // Mock lookup for the rotated token during verification
        when(refreshTokenRepository.findByTokenId(newJti)).thenReturn(Optional.of(newlySavedToken));

        // Act & Assert: Verification succeeds with SHA-256 matching
        RefreshToken verifiedToken = refreshTokenService.verifyRefreshToken(newToken);

        assertNotNull(verifiedToken);
        assertEquals(newJti, verifiedToken.getTokenId());
        assertFalse(verifiedToken.isRevoked());
        assertEquals(invokeHash(newToken), verifiedToken.getHashedToken());
    }

    @Test
    @DisplayName("Should revoke token")
    void shouldRevokeToken() {

        RefreshToken token =
                createValidToken(invokeHash(rawToken));

        when(jwtService.extractJti(rawToken))
                .thenReturn(tokenId);

        when(refreshTokenRepository.findByTokenId(tokenId))
                .thenReturn(Optional.of(token));

        refreshTokenService.revokeToken(rawToken);

        assertTrue(token.isRevoked());

        verify(refreshTokenRepository)
                .save(token);
    }

    @Test
    @DisplayName("Should revoke all user tokens")
    void shouldRevokeAllUserTokens() {

        RefreshToken t1 =
                createValidToken("hash1");

        RefreshToken t2 =
                createValidToken("hash2");

        when(refreshTokenRepository
                     .findByUserIdAndRevokedFalseOrderByCreatedAtAsc(userId))
                .thenReturn(List.of(t1, t2));

        refreshTokenService.revokeAllByUser(userId);

        assertTrue(t1.isRevoked());
        assertTrue(t2.isRevoked());

        verify(refreshTokenRepository)
                .saveAll(anyList());
    }

    @Test
    @DisplayName("Should delete expired tokens")
    void shouldDeleteExpiredTokens() {

        refreshTokenService.deleteExpiredTokens();

        verify(refreshTokenRepository)
                .deleteByExpiryDateBefore(any(Instant.class));
    }

    // =========================
    // Helpers
    // =========================

    private RefreshToken createValidToken(String hash) {

        RefreshToken token = new RefreshToken();

        token.setUserId(userId);
        token.setTokenId(tokenId);
        token.setHashedToken(hash);
        token.setRevoked(false);
        token.setExpiryDate(
                Instant.now().plusSeconds(3600)
        );

        return token;
    }

    private String invokeHash(String rawToken) {

        try {
            var method =
                    RefreshTokenService.class
                            .getDeclaredMethod(
                                    "hashToken",
                                    String.class
                            );

            method.setAccessible(true);

            return (String) method.invoke(
                    refreshTokenService,
                    rawToken
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
