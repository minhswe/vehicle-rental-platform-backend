package com.rentalplatform.backend.wallet.mapper;

import com.rentalplatform.backend.wallet.dto.response.WalletResponse;
import com.rentalplatform.backend.wallet.dto.response.WalletTransactionResponse;
import com.rentalplatform.backend.wallet.entity.Wallet;
import com.rentalplatform.backend.wallet.entity.WalletTransaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WalletMapper {
    WalletResponse toResponse(Wallet wallet);

    WalletTransactionResponse
    toTransactionResponse(
            WalletTransaction transaction
    );



}
