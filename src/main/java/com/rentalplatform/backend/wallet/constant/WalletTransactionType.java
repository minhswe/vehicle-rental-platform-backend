package com.rentalplatform.backend.wallet.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WalletTransactionType {
    TOP_UP("Wallet top-up"),
    PAYMENT("Payment completed"),
    REFUND("Refund processed"),
    HOLD("Amount reserved"),
    RELEASE("Hold released"),
    CONSUMED("Held amount consumed"),
    FEE("Service fee charged"),
    ADJUSTMENT("Manual balance adjustment");


    private final String description;
}
