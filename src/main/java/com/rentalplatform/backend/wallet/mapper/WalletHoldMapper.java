package com.rentalplatform.backend.wallet.mapper;

import com.rentalplatform.backend.wallet.dto.response.WalletHoldResponse;
import com.rentalplatform.backend.wallet.entity.WalletHold;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletHoldMapper {
    @Mapping(
            target = "bookingId",
            source = "booking.id"
    )
    @Mapping(
            target = "paymentId",
            source = "payment.id"
    )
    WalletHoldResponse toResponse(
            WalletHold hold
    );
}
