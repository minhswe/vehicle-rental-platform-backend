package com.rentalplatform.backend.payment.service;

import com.rentalplatform.backend.booking.entity.Booking;
import com.rentalplatform.backend.booking.enums.BookingStatus;
import com.rentalplatform.backend.booking.repository.BookingRepository;
import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import com.rentalplatform.backend.common.security.AuthenticationFacade;
import com.rentalplatform.backend.owner.entity.VehicleOwner;
import com.rentalplatform.backend.payment.dto.request.PaymentRequest;
import com.rentalplatform.backend.payment.dto.response.PaymentResponse;
import com.rentalplatform.backend.payment.entity.Payment;
import com.rentalplatform.backend.payment.enums.Currency;
import com.rentalplatform.backend.payment.enums.PaymentMethod;
import com.rentalplatform.backend.payment.enums.PaymentProvider;
import com.rentalplatform.backend.payment.enums.PaymentStatus;
import com.rentalplatform.backend.payment.mapper.PaymentMapper;
import com.rentalplatform.backend.payment.repository.PaymentRepository;
import com.rentalplatform.backend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UUID customerId;
    private UUID ownerId;

    private User customer;
    private User ownerUser;

    private VehicleOwner owner;

    private Booking booking;
    private Payment payment;

    private PaymentRequest request;
    private PaymentResponse response;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        ownerId = UUID.randomUUID();

        customer = User.builder()
                       .id(customerId)
                       .build();

        ownerUser = User.builder()
                        .id(ownerId)
                        .build();

        owner = VehicleOwner.builder()
                            .user(ownerUser)
                            .build();

        booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setCustomer(customer);
        booking.setOwner(owner);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setTotalAmount(BigDecimal.valueOf(100));

        payment = Payment.builder()
                         .id(UUID.randomUUID())
                         .booking(booking)
                         .amount(BigDecimal.valueOf(100))
                         .currency(Currency.VND)
                         .paymentMethod(PaymentMethod.CARD)
                         .provider(PaymentProvider.STRIPE)
                         .paymentStatus(PaymentStatus.PENDING)
                         .transactionCode("TXN-001")
                         .build();

        request = new PaymentRequest();
        request.setBookingId(booking.getId());
        request.setCurrency(Currency.VND);
        request.setPaymentMethod(PaymentMethod.CARD);
        request.setProvider(PaymentProvider.STRIPE);

        response = PaymentResponse.builder()
                                  .id(payment.getId())
                                  .bookingId(booking.getId())
                                  .build();
    }

    @Test
    @DisplayName("Should create payment successfully")
    void shouldCreatePaymentSuccessfully() {

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(customerId);

        when(paymentRepository.findByBookingId(booking.getId()))
                .thenReturn(Optional.empty());

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(payment);

        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(response);

        PaymentResponse result =
                paymentService.createPayment(request);

        assertNotNull(result);

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw BOOKING_NOT_FOUND when booking does not exist")
    void shouldThrowWhenBookingNotFound() {

        when(bookingRepository.findById(any()))
                .thenReturn(Optional.empty());

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> paymentService.createPayment(request)
                );

        assertEquals(
                ErrorCode.BOOKING_NOT_FOUND,
                ex.getErrorCode()
        );
    }

    @Test

    void shouldThrowWhenPaymentAlreadyExists() {

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(customerId);

        when(paymentRepository.findByBookingId(booking.getId()))
                .thenReturn(Optional.of(payment));

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> paymentService.createPayment(request)
                );

        assertEquals(
                ErrorCode.PAYMENT_ALREADY_EXISTS,
                ex.getErrorCode()
        );
    }

    @Test
    void shouldPaySuccessfully() {

        when(paymentRepository.findById(payment.getId()))
                .thenReturn(Optional.of(payment));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(customerId);

        when(paymentRepository.save(any()))
                .thenReturn(payment);

        when(paymentMapper.toResponse(any()))
                .thenReturn(response);

        PaymentResponse result =
                paymentService.pay(payment.getId());

        assertNotNull(result);

        assertEquals(
                PaymentStatus.PAID,
                payment.getPaymentStatus()
        );

        assertNotNull(payment.getPaidAt());
    }

    @Test
    void shouldReturnWhenAlreadyRefunded() {

        payment.setPaymentStatus(
                PaymentStatus.REFUNDED
        );

        when(paymentRepository.findByBookingId(
                booking.getId()))
                .thenReturn(Optional.of(payment));

        paymentService.refundBookingPayment(
                booking
        );

        verify(paymentRepository, never())
                .save(any());
    }

    @Test
    @DisplayName("Should throw INVALID_BOOKING_STATUS when booking is not pending")
    void shouldThrowWhenBookingNotPending() {

        booking.setBookingStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(customerId);

        AppException ex = assertThrows(
                AppException.class,
                () -> paymentService.createPayment(request)
        );

        assertEquals(
                ErrorCode.INVALID_BOOKING_STATUS,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw INVALID_PAYMENT_PROVIDER when provider is invalid")
    void shouldThrowWhenProviderInvalid() {

        request.setProvider(PaymentProvider.VNPAY);

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(customerId);

        AppException ex = assertThrows(
                AppException.class,
                () -> paymentService.createPayment(request)
        );

        assertEquals(
                ErrorCode.INVALID_PAYMENT_PROVIDER,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw INVALID_PAYMENT_AMOUNT when total amount is null")
    void shouldThrowWhenAmountNull() {

        booking.setTotalAmount(null);

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(customerId);

        when(paymentRepository.findByBookingId(booking.getId()))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(
                AppException.class,
                () -> paymentService.createPayment(request)
        );

        assertEquals(
                ErrorCode.INVALID_PAYMENT_AMOUNT,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw BOOKING_ACCESS_DENIED when current user is not booking customer")
    void shouldThrowWhenCustomerAccessDenied() {

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(UUID.randomUUID());

        AppException ex = assertThrows(
                AppException.class,
                () -> paymentService.createPayment(request)
        );

        assertEquals(
                ErrorCode.BOOKING_ACCESS_DENIED,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should get payment by booking id successfully")
    void shouldGetPaymentByBookingIdSuccessfully() {

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(customerId);

        when(paymentRepository.findByBookingId(booking.getId()))
                .thenReturn(Optional.of(payment));

        when(paymentMapper.toResponse(payment))
                .thenReturn(response);

        PaymentResponse result =
                paymentService.getPaymentByBookingId(
                        booking.getId()
                );

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should throw BOOKING_NOT_FOUND when getting payment by booking id")
    void shouldThrowBookingNotFoundWhenGettingPayment() {
        UUID bookingId = booking.getId();

        when(bookingRepository.findById(any()))
                .thenReturn(Optional.empty());



        AppException ex = assertThrows(
                AppException.class,
                () -> paymentService.getPaymentByBookingId(
                        bookingId
                )
        );

        assertEquals(
                ErrorCode.BOOKING_NOT_FOUND,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw BOOKING_ACCESS_DENIED when user has no access")
    void shouldThrowAccessDeniedWhenGettingPayment() {
        UUID bookingId = booking.getId();

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(UUID.randomUUID());



        AppException ex = assertThrows(
                AppException.class,
                () -> paymentService.getPaymentByBookingId(
                        bookingId
                )
        );

        assertEquals(
                ErrorCode.BOOKING_ACCESS_DENIED,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw PAYMENT_NOT_FOUND when payment does not exist")
    void shouldThrowPaymentNotFoundWhenGettingPayment() {

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(customerId);

        when(paymentRepository.findByBookingId(booking.getId()))
                .thenReturn(Optional.empty());

        UUID bookingId = booking.getId();

        AppException ex = assertThrows(
                AppException.class,
                () -> paymentService.getPaymentByBookingId(
                        bookingId
                )
        );

        assertEquals(
                ErrorCode.PAYMENT_NOT_FOUND,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw PAYMENT_NOT_FOUND when paying non-existing payment")
    void shouldThrowWhenPaymentNotFound() {
        UUID paymentId = payment.getId();

        when(paymentRepository.findById(any()))
                .thenReturn(Optional.empty());



        AppException ex = assertThrows(
                AppException.class,
                () -> paymentService.pay(paymentId)
        );

        assertEquals(
                ErrorCode.PAYMENT_NOT_FOUND,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw INVALID_PAYMENT_STATE when payment is not pending")
    void shouldThrowWhenPaymentNotPending() {

        payment.setPaymentStatus(PaymentStatus.PAID);

        when(paymentRepository.findById(payment.getId()))
                .thenReturn(Optional.of(payment));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(customerId);

        UUID paymentId = payment.getId();

        AppException ex = assertThrows(
                AppException.class,
                () -> paymentService.pay(paymentId)
        );

        assertEquals(
                ErrorCode.INVALID_PAYMENT_STATE,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw BOOKING_ACCESS_DENIED when another user tries to pay")
    void shouldThrowWhenPayAccessDenied() {

        when(paymentRepository.findById(payment.getId()))
                .thenReturn(Optional.of(payment));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(UUID.randomUUID());

        UUID paymentId = payment.getId();
        AppException ex = assertThrows(
                AppException.class,
                () -> paymentService.pay(paymentId)
        );

        assertEquals(
                ErrorCode.BOOKING_ACCESS_DENIED,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should cancel payment successfully")
    void shouldCancelSuccessfully() {

        when(paymentRepository.findById(payment.getId()))
                .thenReturn(Optional.of(payment));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(customerId);

        when(paymentRepository.save(any()))
                .thenReturn(payment);

        when(paymentMapper.toResponse(any()))
                .thenReturn(response);

        PaymentResponse result =
                paymentService.cancel(payment.getId());

        assertNotNull(result);

        assertEquals(
                PaymentStatus.CANCELLED,
                payment.getPaymentStatus()
        );
    }

    @Test
    @DisplayName("Should throw PAYMENT_NOT_FOUND when cancelling non-existing payment")
    void shouldThrowPaymentNotFoundWhenCancel() {

        when(paymentRepository.findById(any()))
                .thenReturn(Optional.empty());

        UUID paymentId = payment.getId();
        AppException ex = assertThrows(
                AppException.class,
                () -> paymentService.cancel(paymentId)
        );

        assertEquals(
                ErrorCode.PAYMENT_NOT_FOUND,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw BOOKING_ACCESS_DENIED when another user cancels payment")
    void shouldThrowAccessDeniedWhenCancel() {

        when(paymentRepository.findById(payment.getId()))
                .thenReturn(Optional.of(payment));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(UUID.randomUUID());

        UUID paymentId = payment.getId();

        AppException ex = assertThrows(
                AppException.class,
                () -> paymentService.cancel(paymentId)
        );

        assertEquals(
                ErrorCode.BOOKING_ACCESS_DENIED,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should refund payment successfully")
    void shouldRefundSuccessfully() {

        payment.setPaymentStatus(PaymentStatus.PAID);

        when(paymentRepository.findByBookingId(
                booking.getId()))
                .thenReturn(Optional.of(payment));

        paymentService.refundBookingPayment(
                booking
        );

        assertEquals(
                PaymentStatus.REFUNDED,
                payment.getPaymentStatus()
        );

        assertNotNull(
                payment.getRefundedAt()
        );

        verify(paymentRepository)
                .save(payment);
    }

    @Test
    @DisplayName("Should throw INVALID_PAYMENT_PROVIDER when CARD has null provider")
    void shouldThrowWhenCardProviderIsNull() {

        request.setPaymentMethod(PaymentMethod.CARD);
        request.setProvider(null);

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(customerId);

        AppException ex = assertThrows(
                AppException.class,
                () -> paymentService.createPayment(request)
        );

        assertEquals(
                ErrorCode.INVALID_PAYMENT_PROVIDER,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should throw INVALID_PAYMENT_PROVIDER when BANK_TRANSFER uses STRIPE")
    void shouldThrowWhenBankTransferUsesStripe() {

        request.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        request.setProvider(PaymentProvider.STRIPE);

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(customerId);

        AppException ex = assertThrows(
                AppException.class,
                () -> paymentService.createPayment(request)
        );

        assertEquals(
                ErrorCode.INVALID_PAYMENT_PROVIDER,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Should set paidAt when payment is paid")
    void shouldSetPaidAtWhenPaying() {

        when(paymentRepository.findById(payment.getId()))
                .thenReturn(Optional.of(payment));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(customerId);

        when(paymentRepository.save(any()))
                .thenReturn(payment);

        when(paymentMapper.toResponse(any()))
                .thenReturn(response);

        paymentService.pay(payment.getId());

        assertNotNull(payment.getPaidAt());
    }

    @Test
    @DisplayName("Should set refundedAt when refunding payment")
    void shouldSetRefundedAt() {

        payment.setPaymentStatus(PaymentStatus.PAID);

        when(paymentRepository.findByBookingId(
                booking.getId()))
                .thenReturn(Optional.of(payment));

        paymentService.refundBookingPayment(
                booking
        );

        assertNotNull(
                payment.getRefundedAt()
        );
    }

    @Test
    @DisplayName("Should save payment once when refund succeeds")
    void shouldSaveOnceWhenRefundSuccess() {

        payment.setPaymentStatus(PaymentStatus.PAID);

        when(paymentRepository.findByBookingId(
                booking.getId()))
                .thenReturn(Optional.of(payment));

        paymentService.refundBookingPayment(
                booking
        );

        verify(paymentRepository)
                .save(payment);
    }


    @Test
    @DisplayName("Should throw INVALID_PAYMENT_PROVIDER when CASH has provider")
    void shouldThrowWhenCashHasProvider() {

        request.setPaymentMethod(PaymentMethod.CASH);
        request.setProvider(PaymentProvider.VNPAY);

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        when(authenticationFacade.getCurrentUserId())
                .thenReturn(customerId);

        assertThrows(
                AppException.class,
                () -> paymentService.createPayment(request)
        );
    }


}
