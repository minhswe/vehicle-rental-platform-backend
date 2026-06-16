package com.rentalplatform.backend.wallet.entity;

import com.rentalplatform.backend.booking.entity.Booking;
import com.rentalplatform.backend.common.entity.AuditEntity;
import com.rentalplatform.backend.payment.entity.Payment;
import com.rentalplatform.backend.wallet.enums.WalletHoldStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet_holds")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletHold extends AuditEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_payment_id")
    private Payment payment;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private WalletHoldStatus status;

    private Instant expiresAt;

}
