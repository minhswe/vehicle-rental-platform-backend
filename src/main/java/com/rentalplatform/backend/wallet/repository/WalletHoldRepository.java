package com.rentalplatform.backend.wallet.repository;

import com.rentalplatform.backend.wallet.entity.WalletHold;
import com.rentalplatform.backend.wallet.enums.WalletHoldStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletHoldRepository extends JpaRepository<WalletHold, UUID> {

    Optional<WalletHold>
    findByBookingId(UUID bookingId);

    Optional<WalletHold>
    findByPaymentId(UUID paymentId);

    List<WalletHold>
    findByStatus(WalletHoldStatus status);

    Boolean existsByBookingIdAndStatus(UUID bookingId, WalletHoldStatus status);
}
