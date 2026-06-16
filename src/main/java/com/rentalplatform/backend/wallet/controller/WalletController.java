package com.rentalplatform.backend.wallet.controller;

import com.rentalplatform.backend.wallet.dto.response.WalletHoldResponse;
import com.rentalplatform.backend.wallet.dto.response.WalletResponse;
import com.rentalplatform.backend.wallet.dto.response.WalletTransactionResponse;
import com.rentalplatform.backend.wallet.service.WalletService;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/me")
    public WalletResponse getCurrentWallet() {
        return walletService.getCurrentWallet();
    }

    @GetMapping("/users/{userId}")
    public WalletResponse getWalletByUserId(
            @PathVariable UUID userId
    ) {
        return walletService.getWalletByUserId(userId);
    }

    @PostMapping("/users/{userId}/top-up")
    public WalletResponse topUp(
            @PathVariable UUID userId,
            @RequestParam
            @DecimalMin(value = "0.01")
            BigDecimal amount
    ) {
        return walletService.topUp(
                userId,
                amount
        );
    }

    @PostMapping("/users/{userId}/refund")
    public WalletResponse refund(
            @PathVariable UUID userId,
            @RequestParam UUID paymentId,
            @RequestParam
            @DecimalMin(value = "0.01")
            BigDecimal amount
    ) {
        return walletService.refund(
                userId,
                paymentId,
                amount
        );
    }

    @PostMapping("/holds")
    public WalletHoldResponse holdAmount(
            @RequestParam UUID userId,
            @RequestParam UUID bookingId,
            @RequestParam UUID paymentId,
            @RequestParam
            @DecimalMin(value = "0.01")
            BigDecimal amount
    ) {
        return walletService.holdAmount(
                userId,
                bookingId,
                paymentId,
                amount
        );
    }

    @PostMapping("/holds/{holdId}/release")
    public WalletHoldResponse releaseHold(
            @PathVariable UUID holdId
    ) {
        return walletService.releaseHold(
                holdId
        );
    }

    @PostMapping("/holds/{holdId}/consume")
    public WalletHoldResponse consumeHold(
            @PathVariable UUID holdId
    ) {
        return walletService.consumeHold(
                holdId
        );
    }

    @GetMapping("/users/{userId}/transactions")
    public Page<WalletTransactionResponse> getWalletTransactions(
            @PathVariable UUID userId,
            Pageable pageable
    ) {
        return walletService.getWalletTransactions(
                userId,
                pageable
        );
    }
}
