package com.rentalplatform.backend.payment.service;

import com.rentalplatform.backend.booking.entity.Booking;
import com.rentalplatform.backend.booking.enums.BookingStatus;
import com.rentalplatform.backend.booking.repository.BookingRepository;
import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import com.rentalplatform.backend.common.security.AuthenticationFacade;
import com.rentalplatform.backend.payment.dto.request.PaymentRequest;
import com.rentalplatform.backend.payment.dto.response.PaymentResponse;
import com.rentalplatform.backend.payment.entity.Payment;
import com.rentalplatform.backend.payment.enums.PaymentMethod;
import com.rentalplatform.backend.payment.enums.PaymentProvider;
import com.rentalplatform.backend.payment.enums.PaymentStatus;
import com.rentalplatform.backend.payment.mapper.PaymentMapper;
import com.rentalplatform.backend.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentMapper paymentMapper;
    private final AuthenticationFacade authenticationFacade;

    @Transactional
    @Override
    public PaymentResponse createPayment(PaymentRequest request) {

        Booking booking = getBooking(request);

        validateCustomerAccess(booking);
        validateBookingCanBePaid(booking);
        validatePaymentProvider(request.getPaymentMethod(), request.getProvider());

        if (paymentRepository.findByBookingId(booking.getId())
                             .isPresent()) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        if (booking.getTotalAmount() == null) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        Payment payment = Payment.builder()
                                 .booking(booking)
                                 .amount(booking.getTotalAmount())
                                 .currency(request.getCurrency())
                                 .paymentMethod(request.getPaymentMethod())
                                 .provider(request.getProvider())
                                 .paymentStatus(PaymentStatus.PENDING)
                                 .transactionCode(generateTransactionCode())
                                 .build();
        try {
            return paymentMapper.toResponse(paymentRepository.save(payment));
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }


    }

    @Override
    public PaymentResponse getPaymentByBookingId(UUID bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                                           .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        validateBookingAccess(booking);

        Payment payment = paymentRepository.findByBookingId(bookingId)
                                           .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        return paymentMapper.toResponse(payment);
    }

    @Transactional
    @Override
    public PaymentResponse pay(UUID paymentId) {

        Payment payment = getPayment(paymentId);

        validateCustomerAccess(payment.getBooking());
        validatePaymentCanBeProcessed(payment);

        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidAt(Instant.now());


        Payment saved = paymentRepository.save(payment);

        return paymentMapper.toResponse(saved);
    }

    @Transactional
    @Override
    public PaymentResponse cancel(UUID paymentId) {

        Payment payment = getPayment(paymentId);

        validateCustomerAccess(payment.getBooking());

        validatePaymentCanBeProcessed(payment);

        payment.setPaymentStatus(PaymentStatus.CANCELLED);

        return paymentMapper.toResponse(paymentRepository.save(payment));
    }


    @Transactional
    @Override
    public void refundBookingPayment(Booking booking) {

        Payment payment = paymentRepository.findByBookingId(booking.getId())
                                           .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            return;
        }

        validatePaidPayment(payment);


        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(
                Instant.now()
        );

        paymentRepository.save(payment);
    }

    //========================================================
    // HELPER METHODS
    //========================================================

    private Payment getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                                .orElseThrow(() -> new AppException((ErrorCode.PAYMENT_NOT_FOUND)));
    }

    private Booking getBooking(PaymentRequest request) {
        return bookingRepository.findById(request.getBookingId())
                                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
    }

    private void validateBookingAccess(Booking booking) {

        UUID currentUserId = authenticationFacade.getCurrentUserId();

        boolean isCustomer = booking.getCustomer()
                                    .getId()
                                    .equals(currentUserId);

        boolean isOwner = booking.getOwner()
                                 .getUser()
                                 .getId()
                                 .equals(currentUserId);

        if (!isCustomer && !isOwner) {
            throw new AppException(ErrorCode.BOOKING_ACCESS_DENIED);
        }
    }

    private void validateCustomerAccess(Booking booking) {

        UUID currentUserId = authenticationFacade.getCurrentUserId();

        if (!booking.getCustomer()
                    .getId()
                    .equals(currentUserId)) {

            throw new AppException(ErrorCode.BOOKING_ACCESS_DENIED);
        }
    }


    private void validatePaymentProvider(PaymentMethod method, PaymentProvider provider) {
        switch (method) {
            case CARD -> {
                if (provider != PaymentProvider.STRIPE) {
                    throw new AppException(ErrorCode.INVALID_PAYMENT_PROVIDER);
                }
            }
            case BANK_TRANSFER -> {
                if (provider != PaymentProvider.VNPAY) {
                    throw new AppException(ErrorCode.INVALID_PAYMENT_PROVIDER);
                }
            }
            case CASH, WALLET -> {
                if (provider != null) {
                    throw new AppException(ErrorCode.INVALID_PAYMENT_PROVIDER);
                }
            }
            default -> {
                // Do nothing
            }
        }
    }

    private void validatePaymentCanBeProcessed(Payment payment) {
        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_STATE);
        }
    }

    private void validateBookingCanBePaid(Booking booking) {
        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_BOOKING_STATUS);
        }
    }

    private void validatePaidPayment(Payment payment) {
        if (payment.getPaymentStatus() != PaymentStatus.PAID) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_STATE);
        }
    }

    private String generateTransactionCode() {
        return UUID.randomUUID()
                   .toString();
    }
}
