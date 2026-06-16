package com.rentalplatform.backend.wallet.dto.response;

import com.rentalplatform.backend.wallet.enums.WalletTransactionStatus;
import com.rentalplatform.backend.wallet.enums.WalletTransactionType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
public class WalletTransactionResponse {

    private UUID id;

    private WalletTransactionType type;

    private BigDecimal amount;

    private WalletTransactionStatus status;

    private String description;

    private Instant createdAt;

}
