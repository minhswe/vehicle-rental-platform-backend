package com.rentalplatform.backend.auth.repository;

import com.rentalplatform.backend.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenId(UUID tokenId);

    List<RefreshToken> findByUserIdAndRevokedFalseOrderByCreatedAtAsc(UUID userId);

    void deleteByExpiryDateBefore(Instant expiryDateBefore);

}
