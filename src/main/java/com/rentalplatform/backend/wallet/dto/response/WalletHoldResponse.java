package com.rentalplatform.backend.wallet.dto.response;

import com.rentalplatform.backend.wallet.enums.WalletHoldStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
public class WalletHoldResponse {
    private UUID id;

    private UUID bookingId;

    private UUID paymentId;

    private BigDecimal amount;

    private WalletHoldStatus status;

    private Instant expiresAt;
}
