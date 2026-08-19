package com.rentalplatform.backend.wallet.service;

import com.rentalplatform.backend.common.security.AuthenticationFacade;
import com.rentalplatform.backend.payment.constant.Currency;
import com.rentalplatform.backend.user.entity.User;
import com.rentalplatform.backend.user.repository.UserRepository;
import com.rentalplatform.backend.wallet.constant.WalletStatus;
import com.rentalplatform.backend.wallet.entity.Wallet;
import com.rentalplatform.backend.wallet.repository.WalletHoldRepository;
import com.rentalplatform.backend.wallet.repository.WalletRepository;
import com.rentalplatform.backend.wallet.repository.WalletTransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class WalletConcurrencyTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private WalletHoldRepository walletHoldRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private AuthenticationFacade authenticationFacade;

    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        walletHoldRepository.deleteAll();
        walletTransactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();

        user = User.builder()
                .email("walletuser@test.com")
                .password("password")
                .build();
        user = userRepository.save(user);

        wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.valueOf(1000))
                .heldBalance(BigDecimal.ZERO)
                .currency(Currency.VND)
                .status(WalletStatus.ACTIVE)
                .build();
        wallet = walletRepository.save(wallet);

        when(authenticationFacade.getCurrentUserId()).thenReturn(user.getId());
    }

    @AfterEach
    void tearDown() {
        walletHoldRepository.deleteAll();
        walletTransactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Simulate 5 concurrent top-up requests to verify zero lost updates with Optimistic Locking and Retry")
    void testConcurrentTopUp_NoLostUpdates() throws InterruptedException {
        int numberOfThreads = 5;
        BigDecimal topUpAmount = BigDecimal.valueOf(100);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await(); // wait for all threads to be ready
                    walletService.topUp(user.getId(), topUpAmount);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("Thread failed with exception: " + e.getClass().getName() + " - " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // trigger all threads simultaneously
        doneLatch.await(); // wait for all to complete

        executorService.shutdown();

        // Verify that all 5 top-up requests succeeded via retry mechanism
        assertEquals(numberOfThreads, successCount.get(), "All 5 topUp requests should succeed through retries");

        // Verify final wallet balance: 1000 + (5 * 100) = 1500 (No Lost Update)
        Wallet updatedWallet = walletRepository.findByUserId(user.getId()).orElseThrow();
        assertEquals(0, BigDecimal.valueOf(1500).compareTo(updatedWallet.getBalance()), "Final balance must reflect all 5 top-up operations exactly without lost updates");
    }
}
