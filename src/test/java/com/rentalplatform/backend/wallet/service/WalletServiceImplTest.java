package com.rentalplatform.backend.wallet.service;

import com.rentalplatform.backend.booking.entity.Booking;
import com.rentalplatform.backend.booking.repository.BookingRepository;
import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import com.rentalplatform.backend.common.security.AuthenticationFacade;
import com.rentalplatform.backend.payment.entity.Payment;
import com.rentalplatform.backend.payment.constant.PaymentStatus;
import com.rentalplatform.backend.payment.repository.PaymentRepository;
import com.rentalplatform.backend.user.entity.User;
import com.rentalplatform.backend.wallet.dto.response.WalletHoldResponse;
import com.rentalplatform.backend.wallet.dto.response.WalletResponse;
import com.rentalplatform.backend.wallet.entity.Wallet;
import com.rentalplatform.backend.wallet.entity.WalletHold;
import com.rentalplatform.backend.wallet.entity.WalletTransaction;
import com.rentalplatform.backend.wallet.constant.WalletHoldStatus;
import com.rentalplatform.backend.wallet.mapper.WalletHoldMapper;
import com.rentalplatform.backend.wallet.mapper.WalletMapper;
import com.rentalplatform.backend.wallet.repository.WalletHoldRepository;
import com.rentalplatform.backend.wallet.repository.WalletRepository;
import com.rentalplatform.backend.wallet.repository.WalletTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private WalletHoldRepository walletHoldRepository;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private WalletMapper walletMapper;

    @Mock
    private WalletHoldMapper walletHoldMapper;

    @InjectMocks
    private WalletServiceImpl walletService;

    private UUID userId;

    private Wallet wallet;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();

        User user = User.builder()
                        .id(userId)
                        .build();

        wallet = Wallet.builder()
                       .id(UUID.randomUUID())
                       .user(user)
                       .balance(BigDecimal.valueOf(1000))
                       .heldBalance(BigDecimal.ZERO)
                       .build();

        ReflectionTestUtils.setField(
                walletService,
                "holdExpirationMinutes",
                30L
        );

        lenient().when(walletRepository.save(any(Wallet.class)))
                 .thenAnswer(invocation -> invocation.getArgument(0));

        lenient().when(walletHoldRepository.save(any(WalletHold.class)))
                 .thenAnswer(invocation -> {
                     WalletHold hold = invocation.getArgument(0);

                     if (hold.getId() == null) {
                         hold.setId(UUID.randomUUID());
                     }

                     return hold;
                 });

        lenient().when(walletTransactionRepository.save(any(WalletTransaction.class)))
                 .thenAnswer(invocation -> {
                     WalletTransaction tx = invocation.getArgument(0);

                     if (tx.getId() == null) {
                         tx.setId(UUID.randomUUID());
                     }

                     return tx;
                 });
    }

    @DisplayName("Top up should increase wallet balance successfully")
    @Test
    void topUp_ShouldIncreaseBalanceSuccessfully() {

        BigDecimal amount = BigDecimal.valueOf(500);

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        WalletResponse response =
                mock(WalletResponse.class);

        when(walletMapper.toResponse(any(Wallet.class)))
                .thenReturn(response);

        WalletResponse result =
                walletService.topUp(
                        userId,
                        amount
                );

        assertNotNull(result);

        assertEquals(
                BigDecimal.valueOf(1500),
                wallet.getBalance()
        );

        verify(walletRepository)
                .save(wallet);

        verify(walletTransactionRepository)
                .save(any(WalletTransaction.class));

        verify(walletMapper)
                .toResponse(wallet);
    }

    @Test
    void topUp_ShouldThrowException_WhenAmountIsNegative() {

        assertThrows(
                AppException.class,
                () -> walletService.topUp(
                        userId,
                        BigDecimal.valueOf(-100)
                )
        );

        verifyNoInteractions(walletRepository);
    }

    @Test
    void topUp_ShouldThrowException_WhenWalletNotFound() {

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> walletService.topUp(
                                userId,
                                BigDecimal.valueOf(100)
                        )
                );

        assertEquals(
                ErrorCode.WALLET_NOT_FOUND,
                ex.getErrorCode()
        );
    }

    @DisplayName("Hold amount should reserve wallet balance successfully")
    @Test
    void holdAmount_ShouldReserveMoneySuccessfully() {

        UUID bookingId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        BigDecimal amount = BigDecimal.valueOf(500);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setCustomer(wallet.getUser());

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setPaymentStatus(PaymentStatus.PENDING);

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        when(walletHoldRepository.existsByBookingIdAndStatus(
                bookingId,
                WalletHoldStatus.HOLD
        )).thenReturn(false);

        WalletHoldResponse response =
                mock(WalletHoldResponse.class);

        when(walletHoldMapper.toResponse(any(WalletHold.class)))
                .thenReturn(response);

        WalletHoldResponse result =
                walletService.holdAmount(
                        userId,
                        bookingId,
                        paymentId,
                        amount
                );

        assertNotNull(result);

        assertEquals(
                BigDecimal.valueOf(500),
                wallet.getHeldBalance()
        );

        verify(walletRepository)
                .save(wallet);

        verify(walletHoldRepository)
                .save(any(WalletHold.class));

        verify(walletTransactionRepository)
                .save(any(WalletTransaction.class));
    }

    @Test
    void holdAmount_ShouldThrowException_WhenBalanceInsufficient() {

        wallet.setBalance(
                BigDecimal.valueOf(100)
        );

        UUID bookingId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Booking booking = new Booking();

        booking.setId(bookingId);
        booking.setCustomer(wallet.getUser());

        Payment payment = new Payment();

        payment.setId(paymentId);
        payment.setBooking(booking);
        payment.setAmount(BigDecimal.valueOf(500));
        payment.setPaymentStatus(PaymentStatus.PENDING);

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        when(walletHoldRepository.existsByBookingIdAndStatus(
                bookingId,
                WalletHoldStatus.HOLD
        )).thenReturn(false);

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> walletService.holdAmount(
                                userId,
                                bookingId,
                                paymentId,
                                BigDecimal.valueOf(500)
                        )
                );

        assertEquals(
                ErrorCode.INSUFFICIENT_WALLET_BALANCE,
                ex.getErrorCode()
        );
    }

    @DisplayName("Release hold should release reserved amount successfully")
    @Test
    void releaseHold_ShouldReleaseMoneySuccessfully() {

        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());

        wallet.setHeldBalance(
                BigDecimal.valueOf(300)
        );

        WalletHold hold =
                WalletHold.builder()
                          .id(UUID.randomUUID())
                          .wallet(wallet)
                          .booking(booking)
                          .amount(BigDecimal.valueOf(300))
                          .status(WalletHoldStatus.HOLD)
                          .build();

        when(walletHoldRepository.findById(hold.getId()))
                .thenReturn(Optional.of(hold));

        WalletHoldResponse response =
                mock(WalletHoldResponse.class);

        when(walletHoldMapper.toResponse(any(WalletHold.class)))
                .thenReturn(response);

        WalletHoldResponse result =
                walletService.releaseHold(
                        hold.getId()
                );

        assertNotNull(result);

        assertEquals(
                BigDecimal.ZERO,
                wallet.getHeldBalance()
        );

        assertEquals(
                WalletHoldStatus.RELEASED,
                hold.getStatus()
        );
    }

    @DisplayName("Consume hold should deduct wallet balance successfully")
    @Test
    void consumeHold_ShouldDeductBalanceSuccessfully() {

        wallet.setBalance(
                BigDecimal.valueOf(1000)
        );

        wallet.setHeldBalance(
                BigDecimal.valueOf(400)
        );

        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setCustomer(wallet.getUser());

        WalletHold hold =
                WalletHold.builder()
                          .id(UUID.randomUUID())
                          .wallet(wallet)
                          .booking(booking)
                          .amount(BigDecimal.valueOf(400))
                          .status(WalletHoldStatus.HOLD)
                          .build();

        when(walletHoldRepository.findById(hold.getId()))
                .thenReturn(Optional.of(hold));

        WalletHoldResponse response =
                mock(WalletHoldResponse.class);

        when(walletHoldMapper.toResponse(any(WalletHold.class)))
                .thenReturn(response);

        WalletHoldResponse result =
                walletService.consumeHold(
                        hold.getId()
                );

        assertNotNull(result);

        assertEquals(
                BigDecimal.valueOf(600),
                wallet.getBalance()
        );

        assertEquals(
                BigDecimal.ZERO,
                wallet.getHeldBalance()
        );

        assertEquals(
                WalletHoldStatus.CONSUMED,
                hold.getStatus()
        );
    }

    @DisplayName("Refund should increase wallet balance successfully")
    @Test
    void refund_ShouldIncreaseBalanceSuccessfully() {

        UUID paymentId = UUID.randomUUID();

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        WalletResponse response =
                mock(WalletResponse.class);

        when(walletMapper.toResponse(any(Wallet.class)))
                .thenReturn(response);

        WalletResponse result =
                walletService.refund(
                        userId,
                        paymentId,
                        BigDecimal.valueOf(300)
                );

        assertNotNull(result);

        assertEquals(
                BigDecimal.valueOf(1300),
                wallet.getBalance()
        );

        verify(walletTransactionRepository)
                .save(any(WalletTransaction.class));
    }

    @Test
    @DisplayName("Get current wallet successfully")
    void getCurrentWallet_ShouldReturnWallet() {
        when(authenticationFacade.getCurrentUserId())
                .thenReturn(userId);

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        WalletResponse response = mock(WalletResponse.class);

        when(walletMapper.toResponse(wallet))
                .thenReturn(response);

        WalletResponse result =
                walletService.getCurrentWallet();

        assertNotNull(result);
    }

    @Test
    @DisplayName("Get wallet by user id successfully")
    void getWalletByUserId_ShouldReturnWallet() {

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        WalletResponse response =
                mock(WalletResponse.class);

        when(walletMapper.toResponse(wallet))
                .thenReturn(response);

        WalletResponse result =
                walletService.getWalletByUserId(userId);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Hold amount should throw exception when payment not found")
    void holdAmount_ShouldThrowException_WhenPaymentNotFound() {

        UUID bookingId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setCustomer(wallet.getUser());

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.empty());

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> walletService.holdAmount(
                                userId,
                                bookingId,
                                paymentId,
                                BigDecimal.valueOf(500)
                        )
                );

        assertEquals(
                ErrorCode.PAYMENT_NOT_FOUND,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Hold amount should throw exception when booking not found")
    void holdAmount_ShouldThrowException_WhenBookingNotFound() {

        UUID bookingId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> walletService.holdAmount(
                                userId,
                                bookingId,
                                paymentId,
                                BigDecimal.valueOf(500)
                        )
                );

        assertEquals(
                ErrorCode.BOOKING_NOT_FOUND,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Hold amount should throw exception when payment status is not pending")
    void holdAmount_ShouldThrowException_WhenPaymentStatusInvalid() {

        UUID bookingId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setCustomer(wallet.getUser());

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setBooking(booking);
        payment.setAmount(BigDecimal.valueOf(500));
        payment.setPaymentStatus(PaymentStatus.PAID);

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> walletService.holdAmount(
                                userId,
                                bookingId,
                                paymentId,
                                BigDecimal.valueOf(500)
                        )
                );

        assertEquals(
                ErrorCode.INVALID_PAYMENT_STATE,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Release hold should throw exception when hold not found")
    void releaseHold_ShouldThrowException_WhenHoldNotFound() {

        UUID holdId = UUID.randomUUID();

        when(walletHoldRepository.findById(holdId))
                .thenReturn(Optional.empty());

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> walletService.releaseHold(holdId)
                );

        assertEquals(
                ErrorCode.WALLET_HOLD_NOT_FOUND,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Hold amount should throw exception when hold already exists")
    void holdAmount_ShouldThrowException_WhenWalletHoldAlreadyExists() {

        UUID bookingId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setCustomer(wallet.getUser());

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setBooking(booking);
        payment.setAmount(BigDecimal.valueOf(500));
        payment.setPaymentStatus(PaymentStatus.PENDING);

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        when(walletHoldRepository.existsByBookingIdAndStatus(
                bookingId,
                WalletHoldStatus.HOLD))
                .thenReturn(true);

        AppException ex = assertThrows(
                AppException.class,
                () -> walletService.holdAmount(
                        userId,
                        bookingId,
                        paymentId,
                        BigDecimal.valueOf(500))
        );

        assertEquals(
                ErrorCode.WALLET_HOLD_ALREADY_EXISTS,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Hold amount should throw exception when payment amount mismatch")
    void holdAmount_ShouldThrowException_WhenPaymentAmountMismatch() {

        UUID bookingId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setCustomer(wallet.getUser());

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setBooking(booking);
        payment.setAmount(BigDecimal.valueOf(1000));
        payment.setPaymentStatus(PaymentStatus.PENDING);

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        AppException ex = assertThrows(
                AppException.class,
                () -> walletService.holdAmount(
                        userId,
                        bookingId,
                        paymentId,
                        BigDecimal.valueOf(500))
        );

        assertEquals(
                ErrorCode.INVALID_PAYMENT_AMOUNT,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Hold amount should throw exception when booking belongs to another customer")
    void holdAmount_ShouldThrowException_WhenBookingAccessDenied() {

        UUID bookingId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        User anotherUser =
                User.builder()
                    .id(UUID.randomUUID())
                    .build();

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setCustomer(anotherUser);

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setBooking(booking);
        payment.setAmount(BigDecimal.valueOf(500));
        payment.setPaymentStatus(PaymentStatus.PENDING);

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        AppException ex = assertThrows(
                AppException.class,
                () -> walletService.holdAmount(
                        userId,
                        bookingId,
                        paymentId,
                        BigDecimal.valueOf(500))
        );

        assertEquals(
                ErrorCode.BOOKING_ACCESS_DENIED,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Hold amount should throw exception when payment does not belong to booking")
    void holdAmount_ShouldThrowException_WhenPaymentBookingMismatch() {

        UUID bookingId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setCustomer(wallet.getUser());

        Booking anotherBooking = new Booking();
        anotherBooking.setId(UUID.randomUUID());

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setBooking(anotherBooking);
        payment.setAmount(BigDecimal.valueOf(500));
        payment.setPaymentStatus(PaymentStatus.PENDING);

        when(walletRepository.findByUserId(userId))
                .thenReturn(Optional.of(wallet));

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        AppException ex = assertThrows(
                AppException.class,
                () -> walletService.holdAmount(
                        userId,
                        bookingId,
                        paymentId,
                        BigDecimal.valueOf(500))
        );

        assertEquals(
                ErrorCode.INVALID_PAYMENT,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Release hold should throw exception when hold status is not HOLD")
    void releaseHold_ShouldThrowException_WhenHoldStatusInvalid() {

        WalletHold hold =
                WalletHold.builder()
                          .id(UUID.randomUUID())
                          .wallet(wallet)
                          .amount(BigDecimal.valueOf(300))
                          .status(WalletHoldStatus.RELEASED)
                          .build();

        when(walletHoldRepository.findById(hold.getId()))
                .thenReturn(Optional.of(hold));

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> walletService.releaseHold(hold.getId())
                );

        assertEquals(
                ErrorCode.INVALID_WALLET_HOLD_STATE,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Release hold should throw exception when held balance is invalid")
    void releaseHold_ShouldThrowException_WhenHeldBalanceInvalid() {

        wallet.setHeldBalance(BigDecimal.valueOf(100));

        WalletHold hold =
                WalletHold.builder()
                          .id(UUID.randomUUID())
                          .wallet(wallet)
                          .amount(BigDecimal.valueOf(300))
                          .status(WalletHoldStatus.HOLD)
                          .build();

        when(walletHoldRepository.findById(hold.getId()))
                .thenReturn(Optional.of(hold));

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> walletService.releaseHold(hold.getId())
                );

        assertEquals(
                ErrorCode.INVALID_WALLET_STATE,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Consume hold should throw exception when hold not found")
    void consumeHold_ShouldThrowException_WhenHoldNotFound() {

        UUID holdId = UUID.randomUUID();

        when(walletHoldRepository.findById(holdId))
                .thenReturn(Optional.empty());

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> walletService.consumeHold(holdId)
                );

        assertEquals(
                ErrorCode.WALLET_HOLD_NOT_FOUND,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Consume hold should throw exception when hold status invalid")
    void consumeHold_ShouldThrowException_WhenHoldStatusInvalid() {

        WalletHold hold =
                WalletHold.builder()
                          .id(UUID.randomUUID())
                          .wallet(wallet)
                          .amount(BigDecimal.valueOf(400))
                          .status(WalletHoldStatus.CONSUMED)
                          .build();

        when(walletHoldRepository.findById(hold.getId()))
                .thenReturn(Optional.of(hold));

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> walletService.consumeHold(hold.getId())
                );

        assertEquals(
                ErrorCode.INVALID_WALLET_HOLD_STATE,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Consume hold should throw exception when wallet balance insufficient")
    void consumeHold_ShouldThrowException_WhenWalletBalanceInsufficient() {

        wallet.setBalance(BigDecimal.valueOf(100));
        wallet.setHeldBalance(BigDecimal.valueOf(400));

        WalletHold hold =
                WalletHold.builder()
                          .id(UUID.randomUUID())
                          .wallet(wallet)
                          .amount(BigDecimal.valueOf(400))
                          .status(WalletHoldStatus.HOLD)
                          .build();

        when(walletHoldRepository.findById(hold.getId()))
                .thenReturn(Optional.of(hold));

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> walletService.consumeHold(hold.getId())
                );

        assertEquals(
                ErrorCode.INSUFFICIENT_WALLET_BALANCE,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Consume hold should throw exception when held balance invalid")
    void consumeHold_ShouldThrowException_WhenHeldBalanceInvalid() {

        wallet.setBalance(BigDecimal.valueOf(1000));
        wallet.setHeldBalance(BigDecimal.valueOf(100));

        WalletHold hold =
                WalletHold.builder()
                          .id(UUID.randomUUID())
                          .wallet(wallet)
                          .amount(BigDecimal.valueOf(400))
                          .status(WalletHoldStatus.HOLD)
                          .build();

        when(walletHoldRepository.findById(hold.getId()))
                .thenReturn(Optional.of(hold));

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> walletService.consumeHold(hold.getId())
                );

        assertEquals(
                ErrorCode.INVALID_WALLET_STATE,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Top up should throw exception when amount is zero")
    void topUp_ShouldThrowException_WhenAmountIsZero() {

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> walletService.topUp(
                                userId,
                                BigDecimal.ZERO)
                );

        assertEquals(
                ErrorCode.INVALID_AMOUNT,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Top up should throw exception when amount is null")
    void topUp_ShouldThrowException_WhenAmountIsNull() {

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> walletService.topUp(
                                userId,
                                null)
                );

        assertEquals(
                ErrorCode.INVALID_AMOUNT,
                ex.getErrorCode()
        );
    }

    @Test
    @DisplayName("Refund should throw exception when amount is invalid")
    void refund_ShouldThrowException_WhenAmountInvalid() {

        AppException ex =
                assertThrows(
                        AppException.class,
                        () -> walletService.refund(
                                userId,
                                UUID.randomUUID(),
                                BigDecimal.ZERO)
                );

        assertEquals(
                ErrorCode.INVALID_AMOUNT,
                ex.getErrorCode()
        );
    }
}