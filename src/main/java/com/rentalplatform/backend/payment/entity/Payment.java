package com.rentalplatform.backend.payment.entity;

import com.rentalplatform.backend.booking.entity.Booking;
import com.rentalplatform.backend.common.entity.AuditEntity;
import com.rentalplatform.backend.payment.enums.Currency;
import com.rentalplatform.backend.payment.enums.PaymentMethod;
import com.rentalplatform.backend.payment.enums.PaymentProvider;
import com.rentalplatform.backend.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentProvider provider;

    @Column(
            nullable = false,
            unique = true
    )
    private String transactionCode;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private Instant paidAt;

    private Instant refundedAt;
}
