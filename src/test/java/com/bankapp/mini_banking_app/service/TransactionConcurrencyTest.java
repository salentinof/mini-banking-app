package com.bankapp.mini_banking_app.service;

import com.bankapp.mini_banking_app.dto.request.WithdrawRequest;
import com.bankapp.mini_banking_app.entity.Account;
import com.bankapp.mini_banking_app.entity.User;
import com.bankapp.mini_banking_app.exception.InsufficientFundsException;
import com.bankapp.mini_banking_app.repository.AccountRepository;
import com.bankapp.mini_banking_app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Runs against the real (H2) database: concurrent withdrawals on the same account
 * must never overdraw it, thanks to optimistic locking (@Version on Account).
 */
@SpringBootTest
class TransactionConcurrencyTest {

    private static final int THREADS = 10;
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("100.00");
    private static final BigDecimal WITHDRAW_AMOUNT = new BigDecimal("20.00");

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void withdraw_shouldNeverOverdraw_whenRequestsAreConcurrent() throws Exception {
        String accountNumber = givenAccountWithBalance(INITIAL_BALANCE);

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch startSignal = new CountDownLatch(1);
        List<Future<Outcome>> results = new ArrayList<>();

        for (int i = 0; i < THREADS; i++) {
            results.add(executor.submit(() -> {
                startSignal.await();   // every thread waits here, then all start together
                try {
                    transactionService.withdraw(new WithdrawRequest(accountNumber, WITHDRAW_AMOUNT));
                    return Outcome.SUCCESS;
                } catch (ObjectOptimisticLockingFailureException e) {
                    return Outcome.CONFLICT;
                } catch (InsufficientFundsException e) {
                    return Outcome.INSUFFICIENT_FUNDS;
                }
            }));
        }

        startSignal.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS), "withdrawals did not finish in time");

        long successes = 0;
        for (Future<Outcome> result : results) {
            if (result.get() == Outcome.SUCCESS) {
                successes++;
            }
        }

        BigDecimal finalBalance = accountRepository.findByAccountNumber(accountNumber).orElseThrow().getBalance();
        BigDecimal withdrawn = WITHDRAW_AMOUNT.multiply(BigDecimal.valueOf(successes));

        assertTrue(finalBalance.signum() >= 0, "balance went negative: " + finalBalance);
        assertEquals(0, INITIAL_BALANCE.compareTo(finalBalance.add(withdrawn)),
                "money was lost or created: withdrawn=" + withdrawn + ", finalBalance=" + finalBalance);
    }

    private String givenAccountWithBalance(BigDecimal balance) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        User user = new User();
        user.setName("Concurrency Test");
        user.setEmail("concurrency-" + suffix + "@test.com");
        user.setPassword("not-used");
        user = userRepository.save(user);

        Account account = new Account();
        account.setAccountNumber("ACC-CONC-" + suffix);
        account.setBalance(balance);
        account.setUser(user);
        accountRepository.save(account);

        return account.getAccountNumber();
    }

    private enum Outcome { SUCCESS, CONFLICT, INSUFFICIENT_FUNDS }
}
