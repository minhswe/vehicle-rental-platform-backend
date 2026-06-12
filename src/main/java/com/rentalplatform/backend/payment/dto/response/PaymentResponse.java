package com.rentalplatform.backend.payment.dto.response;

import com.rentalplatform.backend.payment.enums.Currency;
import com.rentalplatform.backend.payment.enums.PaymentMethod;
import com.rentalplatform.backend.payment.enums.PaymentProvider;
import com.rentalplatform.backend.payment.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
public class PaymentResponse {

    private UUID id;

    private UUID bookingId;

    private BigDecimal amount;

    private Currency currency;

    private PaymentMethod paymentMethod;

    private PaymentProvider provider;

    private String transactionCode;

    private PaymentStatus paymentStatus;

    private Instant paidAt;

}
