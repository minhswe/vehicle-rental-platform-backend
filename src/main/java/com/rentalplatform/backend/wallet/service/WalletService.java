package com.rentalplatform.backend.wallet.service;

import com.rentalplatform.backend.wallet.dto.response.WalletHoldResponse;
import com.rentalplatform.backend.wallet.dto.response.WalletResponse;
import com.rentalplatform.backend.wallet.dto.response.WalletTransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletService {

    WalletResponse getCurrentWallet();

    WalletResponse getWalletByUserId(UUID userId);

    WalletResponse topUp(
            UUID userId,
            BigDecimal amount
    );

    WalletHoldResponse holdAmount(
            UUID userId,
            UUID bookingId,
            UUID paymentId,
            BigDecimal amount
    );

    WalletHoldResponse releaseHold(
            UUID holdId
    );

    WalletHoldResponse consumeHold(
            UUID holdId
    );

    WalletResponse refund(
            UUID userId,
            UUID paymentId,
            BigDecimal amount
    );

    Page<WalletTransactionResponse>
    getWalletTransactions(
            UUID userId,
            Pageable pageable
    );
}
