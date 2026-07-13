package com.rentalplatform.backend.wallet.dto.response;

import com.rentalplatform.backend.payment.constant.Currency;
import com.rentalplatform.backend.wallet.constant.WalletStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
public class WalletResponse {

    private UUID id;

    private BigDecimal balance;

    private Currency currency;

    private WalletStatus status;
}
