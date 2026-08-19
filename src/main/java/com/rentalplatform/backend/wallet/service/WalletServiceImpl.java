package com.rentalplatform.backend.wallet.service;

import com.rentalplatform.backend.booking.entity.Booking;
import com.rentalplatform.backend.booking.repository.BookingRepository;
import com.rentalplatform.backend.common.exception.AppException;
import com.rentalplatform.backend.common.exception.ErrorCode;
import com.rentalplatform.backend.common.security.AuthenticationFacade;
import com.rentalplatform.backend.payment.entity.Payment;
import com.rentalplatform.backend.payment.constant.PaymentStatus;
import com.rentalplatform.backend.payment.repository.PaymentRepository;
import com.rentalplatform.backend.wallet.dto.response.WalletHoldResponse;
import com.rentalplatform.backend.wallet.dto.response.WalletResponse;
import com.rentalplatform.backend.wallet.dto.response.WalletTransactionResponse;
import com.rentalplatform.backend.wallet.entity.Wallet;
import com.rentalplatform.backend.wallet.entity.WalletHold;
import com.rentalplatform.backend.wallet.entity.WalletTransaction;
import com.rentalplatform.backend.wallet.constant.WalletHoldStatus;
import com.rentalplatform.backend.wallet.constant.WalletReferenceType;
import com.rentalplatform.backend.wallet.constant.WalletTransactionStatus;
import com.rentalplatform.backend.wallet.constant.WalletTransactionType;
import com.rentalplatform.backend.wallet.exception.ConcurrentWalletOperationException;
import com.rentalplatform.backend.wallet.mapper.WalletHoldMapper;
import com.rentalplatform.backend.wallet.mapper.WalletMapper;
import com.rentalplatform.backend.wallet.repository.WalletHoldRepository;
import com.rentalplatform.backend.wallet.repository.WalletRepository;
import com.rentalplatform.backend.wallet.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    private final WalletTransactionRepository walletTransactionRepository;

    private final WalletHoldRepository walletHoldRepository;

    private final AuthenticationFacade authenticationFacade;

    private final BookingRepository bookingRepository;

    private final PaymentRepository paymentRepository;

    private final WalletMapper walletMapper;

    private final WalletHoldMapper walletHoldMapper;

    @Value("${wallet.hold-expiration-minutes}")
    private long holdExpirationMinutes;


    @Transactional(readOnly = true)
    @Override
    public WalletResponse getCurrentWallet() {

        UUID userId =
                authenticationFacade.getCurrentUserId();

        return walletMapper.toResponse(
                getWallet(userId)
        );
    }

    @Transactional(readOnly = true)
    @Override
    public WalletResponse getWalletByUserId(UUID userId) {
        return walletMapper.toResponse(
                getWallet(userId));
    }


    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 50, maxDelay = 500, multiplier = 2, random = true)
    )
    @Override
    public WalletResponse topUp(UUID userId, BigDecimal amount) {


        validateAmount(amount);

        BigDecimal normalizedAmount =
                normalize(amount);

        log.info(
                "Wallet top up requested. userId={}, amount={}",
                userId,
                normalizedAmount
        );

        Wallet wallet = getWallet(userId);

        BigDecimal before =
                wallet.getBalance();

        wallet.setBalance(
                before.add(normalizedAmount)
        );

        walletRepository.save(wallet);

        createTransaction(
                wallet,
                WalletTransactionType.TOP_UP,
                normalizedAmount,
                before,
                wallet.getBalance(),
                WalletReferenceType.SYSTEM,
                null
        );

        log.info(
                "Wallet topped up successfully. walletId={}, balanceBefore={}, balanceAfter={}, amount={}",
                wallet.getId(),
                before,
                wallet.getBalance(),
                normalizedAmount
        );

        return walletMapper.toResponse(wallet);

    }

    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 50, maxDelay = 500, multiplier = 2, random = true)
    )
    @Override
    public WalletHoldResponse holdAmount(UUID userId, UUID bookingId, UUID paymentId, BigDecimal amount) {

        validateAmount(amount);

        BigDecimal normalizedAmount =
                normalize(amount);

        log.info(
                "Wallet hold requested. userId={}, bookingId={}, paymentId={}, amount={}",
                userId,
                bookingId,
                paymentId,
                normalizedAmount
        );

        Wallet wallet = getWallet(userId);

        Booking booking =
                bookingRepository.findById(bookingId)
                                 .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        Payment payment =
                paymentRepository.findById(paymentId)
                                 .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getPaymentStatus()
            != PaymentStatus.PENDING) {

            throw new AppException(
                    ErrorCode.INVALID_PAYMENT_STATE
            );
        }

        if (!payment.getBooking()
                    .getId()
                    .equals(bookingId)) {

            throw new AppException(
                    ErrorCode.INVALID_PAYMENT
            );
        }

        if (!booking.getCustomer()
                    .getId()
                    .equals(userId)) {

            throw new AppException(
                    ErrorCode.BOOKING_ACCESS_DENIED
            );
        }

        if (!wallet.getUser()
                   .getId()
                   .equals(userId)) {

            throw new AppException(
                    ErrorCode.WALLET_ACCESS_DENIED
            );
        }

        if (
                payment.getAmount()
                       .compareTo(normalizedAmount)
                != 0
        ) {
            throw new AppException(
                    ErrorCode.INVALID_PAYMENT_AMOUNT
            );
        }

        BigDecimal available =
                wallet.getBalance()
                      .subtract(
                              wallet.getHeldBalance()
                      );

        boolean existsWalletHold =
                walletHoldRepository
                        .existsByBookingIdAndStatus(
                                bookingId,
                                WalletHoldStatus.HOLD
                        );

        if (existsWalletHold) {
            log.warn(
                    "Wallet hold already exists. bookingId={}",
                    bookingId
            );

            throw new AppException(
                    ErrorCode.WALLET_HOLD_ALREADY_EXISTS
            );
        }


        if (available.compareTo(normalizedAmount) < 0) {

            log.warn(
                    "Insufficient wallet balance. walletId={}, available={}, requested={}",
                    wallet.getId(),
                    available,
                    normalizedAmount
            );

            throw new AppException(
                    ErrorCode.INSUFFICIENT_WALLET_BALANCE
            );
        }

        wallet.setHeldBalance(
                wallet.getHeldBalance()
                      .add(normalizedAmount)
        );

        walletRepository.save(wallet);

        WalletHold hold =
                WalletHold.builder()
                          .wallet(wallet)
                          .booking(booking)
                          .payment(payment)
                          .amount(normalizedAmount)
                          .status(WalletHoldStatus.HOLD)
                          .expiresAt(
                                  Instant.now()
                                         .plus(
                                                 holdExpirationMinutes,
                                                 ChronoUnit.MINUTES
                                         )
                          )
                          .build();


        walletHoldRepository.save(hold);

        createTransaction(
                wallet,
                WalletTransactionType.HOLD,
                normalizedAmount,
                wallet.getBalance(),
                wallet.getBalance(),
                WalletReferenceType.PAYMENT,
                paymentId
        );

        log.info(
                "Wallet amount reserved successfully. holdId={}, walletId={}, amount={}",
                hold.getId(),
                wallet.getId(),
                normalizedAmount
        );

        return walletHoldMapper.toResponse(hold);
    }

    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 50, maxDelay = 500, multiplier = 2, random = true)
    )
    @Override
    public WalletHoldResponse releaseHold(UUID holdId) {
        log.info(
                "Release wallet hold requested. holdId={}",
                holdId
        );

        WalletHold hold =
                walletHoldRepository.findById(holdId)
                                    .orElseThrow(() ->
                                                         new AppException(
                                                                 ErrorCode.WALLET_HOLD_NOT_FOUND
                                                         ));

        if (hold.getStatus()
            != WalletHoldStatus.HOLD) {

            throw new AppException(
                    ErrorCode.INVALID_WALLET_HOLD_STATE
            );
        }


        Wallet wallet =
                hold.getWallet();

        if (wallet.getHeldBalance()
                  .compareTo(hold.getAmount()) < 0) {

            throw new AppException(
                    ErrorCode.INVALID_WALLET_STATE
            );
        }

        wallet.setHeldBalance(
                wallet.getHeldBalance()
                      .subtract(
                              hold.getAmount()
                      )
        );

        walletRepository.save(wallet);

        hold.setStatus(
                WalletHoldStatus.RELEASED
        );

        walletHoldRepository.save(hold);

        createTransaction(
                wallet,
                WalletTransactionType.RELEASE,
                hold.getAmount(),
                wallet.getBalance(),
                wallet.getBalance(),
                WalletReferenceType.BOOKING,
                hold.getBooking()
                    .getId()
        );

        log.info(
                "Wallet hold released successfully. holdId={}, walletId={}, amount={}",
                hold.getId(),
                wallet.getId(),
                hold.getAmount()
        );

        return walletHoldMapper.toResponse(hold);
    }

    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 50, maxDelay = 500, multiplier = 2, random = true)
    )
    @Override
    public WalletHoldResponse consumeHold(UUID holdId) {

        log.info(
                "Consume wallet hold requested. holdId={}",
                holdId
        );

        WalletHold hold =
                walletHoldRepository.findById(holdId)
                                    .orElseThrow(() ->
                                                         new AppException(
                                                                 ErrorCode.WALLET_HOLD_NOT_FOUND
                                                         ));

        if (hold.getStatus()
            != WalletHoldStatus.HOLD) {

            throw new AppException(
                    ErrorCode.INVALID_WALLET_HOLD_STATE
            );
        }


        Wallet wallet =
                hold.getWallet();

        if (wallet.getBalance()
                  .compareTo(hold.getAmount()) < 0) {
            throw new AppException(
                    ErrorCode.INSUFFICIENT_WALLET_BALANCE
            );
        }

        if (wallet.getHeldBalance()
                  .compareTo(hold.getAmount()) < 0) {

            throw new AppException(
                    ErrorCode.INVALID_WALLET_STATE
            );
        }

        BigDecimal before =
                wallet.getBalance();

        wallet.setBalance(
                wallet.getBalance()
                      .subtract(
                              hold.getAmount()
                      )
        );

        wallet.setHeldBalance(
                wallet.getHeldBalance()
                      .subtract(
                              hold.getAmount()
                      )
        );

        walletRepository.save(wallet);

        hold.setStatus(
                WalletHoldStatus.CONSUMED
        );

        walletHoldRepository.save(hold);

        createTransaction(
                wallet,
                WalletTransactionType.PAYMENT,
                hold.getAmount(),
                before,
                wallet.getBalance(),
                WalletReferenceType.BOOKING,
                hold.getBooking()
                    .getId()
        );

        log.info(
                "Wallet hold consumed successfully. holdId={}, walletId={}, amount={}",
                hold.getId(),
                wallet.getId(),
                hold.getAmount()
        );

        return walletHoldMapper.toResponse(hold);
    }

    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 50, maxDelay = 500, multiplier = 2, random = true)
    )
    @Override
    public WalletResponse refund(UUID userId, UUID paymentId, BigDecimal amount) {

        validateAmount(amount);

        BigDecimal normalizedAmount =
                normalize(amount);

        log.info(
                "Refund requested. userId={}, paymentId={}, amount={}",
                userId,
                paymentId,
                normalizedAmount
        );

        Wallet wallet = getWallet(userId);

        BigDecimal before =
                wallet.getBalance();

        wallet.setBalance(
                wallet.getBalance()
                      .add(normalizedAmount)
        );

        walletRepository.save(wallet);

        createTransaction(
                wallet,
                WalletTransactionType.REFUND,
                normalizedAmount,
                before,
                wallet.getBalance(),
                WalletReferenceType.PAYMENT,
                paymentId
        );

        log.info(
                "Refund completed. walletId={}, amount={}, balanceAfter={}",
                wallet.getId(),
                normalizedAmount,
                wallet.getBalance()
        );

        return walletMapper.toResponse(wallet);
    }

    // =============== RECOVERY METHODS FOR RETRY EXHAUSTION ===============

    @Recover
    public WalletResponse recoverTopUp(ObjectOptimisticLockingFailureException ex, UUID userId, BigDecimal amount) {
        log.error("Optimistic locking failure during wallet topUp after retries. userId={}, amount={}", userId, amount, ex);
        throw new ConcurrentWalletOperationException("Concurrent update conflict during wallet topUp for user: " + userId, ex);
    }

    @Recover
    public WalletHoldResponse recoverHoldAmount(ObjectOptimisticLockingFailureException ex, UUID userId, UUID bookingId, UUID paymentId, BigDecimal amount) {
        log.error("Optimistic locking failure during wallet holdAmount after retries. userId={}, bookingId={}", userId, bookingId, ex);
        throw new ConcurrentWalletOperationException("Concurrent update conflict during wallet hold for user: " + userId, ex);
    }

    @Recover
    public WalletHoldResponse recoverHoldOperation(ObjectOptimisticLockingFailureException ex, UUID holdId) {
        log.error("Optimistic locking failure during wallet hold update after retries. holdId={}", holdId, ex);
        throw new ConcurrentWalletOperationException("Concurrent update conflict during wallet hold operation for holdId: " + holdId, ex);
    }

    @Recover
    public WalletResponse recoverRefund(ObjectOptimisticLockingFailureException ex, UUID userId, UUID paymentId, BigDecimal amount) {
        log.error("Optimistic locking failure during wallet refund after retries. userId={}, paymentId={}", userId, paymentId, ex);
        throw new ConcurrentWalletOperationException("Concurrent update conflict during wallet refund for user: " + userId, ex);
    }

    @Override
    public Page<WalletTransactionResponse> getWalletTransactions(UUID userId, Pageable pageable) {
        log.info(
                "Get wallet transactions. userId={}, page={}, size={}",
                userId,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        Wallet wallet =
                getWallet(userId);

        return walletTransactionRepository
                .findByWalletId(
                        wallet.getId(),
                        pageable
                )
                .map(walletMapper::toTransactionResponse);
    }

    // =============== HELPER METHODS ===============
    private Wallet getWallet(UUID userId) {
        return walletRepository.findByUserId(userId)
                               .orElseThrow(() -> {
                                   log.warn(
                                           "Wallet not found. userId={}",
                                           userId
                                   );

                                   return new AppException(
                                           ErrorCode.WALLET_NOT_FOUND
                                   );
                               });
    }

    private WalletTransaction createTransaction(
            Wallet wallet,
            WalletTransactionType type,
            BigDecimal amount,
            BigDecimal before,
            BigDecimal after,
            WalletReferenceType referenceType,
            UUID referenceId
    ) {

        WalletTransaction transaction =
                WalletTransaction.builder()
                                 .wallet(wallet)
                                 .type(type)
                                 .amount(amount)
                                 .status(WalletTransactionStatus.SUCCESS)
                                 .referenceType(referenceType)
                                 .referenceId(referenceId)
                                 .balanceBefore(before)
                                 .balanceAfter(after)
                                 .description(type.getDescription())
                                 .build();

        WalletTransaction saved =
                walletTransactionRepository.save(transaction);

        log.debug(
                "Wallet transaction created. transactionId={}, type={}, amount={}, referenceId={}",
                saved.getId(),
                saved.getType(),
                saved.getAmount(),
                saved.getReferenceId()
        );

        return saved;

    }

    private BigDecimal normalize(
            BigDecimal amount
    ) {
        return amount.setScale(
                0,
                RoundingMode.UNNECESSARY
        );
    }

    private void validateAmount(
            BigDecimal amount
    ) {

        if (amount == null ||
            amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new AppException(
                    ErrorCode.INVALID_AMOUNT
            );
        }
    }
}
