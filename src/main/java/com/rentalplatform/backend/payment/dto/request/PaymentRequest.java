package com.rentalplatform.backend.payment.dto.request;

import com.rentalplatform.backend.payment.constant.Currency;
import com.rentalplatform.backend.payment.constant.PaymentMethod;
import com.rentalplatform.backend.payment.constant.PaymentProvider;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class PaymentRequest {
    private UUID bookingId;

    private BigDecimal amount;

    private Currency currency;

    @NotNull
    private PaymentMethod paymentMethod;

    private PaymentProvider provider;
}
