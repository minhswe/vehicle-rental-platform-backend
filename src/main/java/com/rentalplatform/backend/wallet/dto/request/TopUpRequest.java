package com.rentalplatform.backend.wallet.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TopUpRequest {

    @Positive
    private BigDecimal amount;
}
