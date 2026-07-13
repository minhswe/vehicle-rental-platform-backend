package com.rentalplatform.backend.wallet.entity;

import com.rentalplatform.backend.common.entity.AuditEntity;
import com.rentalplatform.backend.wallet.constant.WalletReferenceType;
import com.rentalplatform.backend.wallet.constant.WalletTransactionStatus;
import com.rentalplatform.backend.wallet.constant.WalletTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "wallet_transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction extends AuditEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    private WalletTransactionType type;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private WalletTransactionStatus status;

    @Enumerated(EnumType.STRING)
    private WalletReferenceType referenceType;

    private UUID referenceId;

    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;

    private String description;
}
