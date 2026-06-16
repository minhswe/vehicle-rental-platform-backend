package com.rentalplatform.backend.wallet.entity;

import com.rentalplatform.backend.common.entity.AuditEntity;
import com.rentalplatform.backend.payment.enums.Currency;
import com.rentalplatform.backend.user.entity.User;
import com.rentalplatform.backend.wallet.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet extends AuditEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false)
    private BigDecimal heldBalance;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    private WalletStatus status;
}
