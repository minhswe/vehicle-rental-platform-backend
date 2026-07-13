package com.rentalplatform.backend.payment.repository;

import com.rentalplatform.backend.payment.entity.Payment;
import com.rentalplatform.backend.payment.constant.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByBookingId(UUID bookingId);

    List<Payment> findByPaymentStatus(PaymentStatus status);

    Optional<Payment> findById(UUID paymentId);

}
