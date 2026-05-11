package com.rentalplatform.backend.auth.service;

import com.rentalplatform.backend.auth.entity.RefreshToken;
import com.rentalplatform.backend.auth.repository.RefreshTokenRepository;
import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import com.rentalplatform.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {
    //Validate tokens
    //Check expiration
    //Rotate token
    //Revoke tokens
    //Logout logic

    private final RefreshTokenRepository refreshTokenRepository;

    //expiry (7 days)
    private final static long REFRESH_TOKEN_DURATION = 7 * 24 * 60 * 60;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    //=== SAVE TOKEN ===
    public RefreshToken createRefreshToken(UUID userId, String rawToken, String device, String ipAddress) {
        RefreshToken refreshToken = new RefreshToken();

        String hashedToken = passwordEncoder.encode(rawToken);
        UUID tokenId = jwtService.extractJti(rawToken);

        refreshToken.setUserId(userId);
        refreshToken.setTokenId(tokenId);
        refreshToken.setHashedToken(hashedToken);
        refreshToken.setDevice(device);
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setRevoked(false);
        refreshToken.setExpiryDate(Instant.now()
                .plusSeconds(REFRESH_TOKEN_DURATION));

        return refreshTokenRepository.save(refreshToken);
    }

    // === VERIFY TOKEN ===
    public RefreshToken verifyRefreshToken(String rawToken) {

        UUID tokenId = jwtService.extractJti(rawToken);

        RefreshToken refreshToken =
                refreshTokenRepository.findByTokenId(tokenId)
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if (refreshToken.isRevoked()) {
            throw new AppException(ErrorCode.TOKEN_REVOKED);
        }

        if (refreshToken.getExpiryDate()
                .isBefore(Instant.now())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        if (!passwordEncoder.matches(rawToken, refreshToken.getHashedToken())){
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        return refreshToken;
    }

    // === ROTATE TOKEN (REFRESH)
    public String rotateRefreshToken(String rawOldToken, String rawNewToken) {
        RefreshToken existing = verifyRefreshToken(rawOldToken);

        //revoke old token
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        //create new token
        String hashedToken = passwordEncoder.encode(rawNewToken);
        UUID tokenId =  jwtService.extractJti(rawNewToken);

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setUserId(existing.getUserId());
        newRefreshToken.setTokenId(tokenId);
        newRefreshToken.setHashedToken(hashedToken);
        newRefreshToken.setDevice(existing.getDevice());
        newRefreshToken.setIpAddress(existing.getIpAddress());
        newRefreshToken.setRevoked(false);
        newRefreshToken.setExpiryDate(Instant.now()
                .plusSeconds(REFRESH_TOKEN_DURATION));

        refreshTokenRepository.save(newRefreshToken);
        return rawNewToken;
    }

    //=== DELETE 1 TOKEN (LOG OUT DEVICE) ===
    public void revokeToken(String token) {
        UUID tokenId = jwtService.extractJti(token);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    public void revokeAllByUser(UUID userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserIdAndRevokedFalseOrderByCreatedAtAsc(userId);
        tokens.forEach((token -> token.setRevoked(true)));
        refreshTokenRepository.saveAll(tokens);
    }

    //=== CLEAN EXPIRED TOKENS (CRON JOB)
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(Instant.now());
    }
}
