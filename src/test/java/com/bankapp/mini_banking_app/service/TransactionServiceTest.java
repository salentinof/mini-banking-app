package com.bankapp.mini_banking_app.service;

import com.bankapp.mini_banking_app.dto.request.TransactionRequest;
import com.bankapp.mini_banking_app.entity.Account;
import com.bankapp.mini_banking_app.entity.Transaction;
import com.bankapp.mini_banking_app.entity.TransactionType;
import com.bankapp.mini_banking_app.exception.AccountNotFoundException;
import com.bankapp.mini_banking_app.exception.InsufficientFundsException;
import com.bankapp.mini_banking_app.exception.SameAccountTransferException;
import com.bankapp.mini_banking_app.repository.AccountRepository;
import com.bankapp.mini_banking_app.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private TransactionRepository transactionRepo;

    @InjectMocks
    private TransactionService service;

    // ---------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------

    private Account account(String number, int balance) {
        Account acc = new Account();
        acc.setAccountNumber(number);
        acc.setBalance(BigDecimal.valueOf(balance));
        return acc;
    }

    private TransactionRequest req(TransactionType type, String from, String to, int amount) {
        TransactionRequest r = new TransactionRequest();
        r.setType(type);
        r.setFromAccount(from);
        r.setToAccount(to);
        r.setAccountNumber(from);
        r.setAmount(BigDecimal.valueOf(amount));
        return r;
    }

    // ---------------------------------------------------------
    // DEPOSIT
    // ---------------------------------------------------------
    @Test
    void execute_shouldDepositMoney() {
        TransactionRequest request = req(TransactionType.DEPOSIT, "A1", null, 100);

        Account acc = account("A1", 50);

        when(accountRepo.findByAccountNumber("A1")).thenReturn(Optional.of(acc));
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction tx = service.execute(request);

        assertEquals(BigDecimal.valueOf(150), acc.getBalance());
        assertEquals(TransactionType.DEPOSIT, tx.getType());
    }

    // ---------------------------------------------------------
    // WITHDRAW
    // ---------------------------------------------------------
    @Test
    void execute_shouldWithdrawMoney() {
        TransactionRequest request = req(TransactionType.WITHDRAW, "A1", null, 30);

        Account acc = account("A1", 100);

        when(accountRepo.findByAccountNumber("A1")).thenReturn(Optional.of(acc));
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction tx = service.execute(request);

        assertEquals(BigDecimal.valueOf(70), acc.getBalance());
        assertEquals(TransactionType.WITHDRAW, tx.getType());
    }

    @Test
    void execute_shouldThrow_whenWithdrawInsufficientFunds() {
        TransactionRequest request = req(TransactionType.WITHDRAW, "A1", null, 200);

        Account acc = account("A1", 50);

        when(accountRepo.findByAccountNumber("A1")).thenReturn(Optional.of(acc));

        assertThrows(InsufficientFundsException.class, () -> service.execute(request));
    }

    // ---------------------------------------------------------
    // TRANSFER
    // ---------------------------------------------------------
    @Test
    void execute_shouldTransferMoney() {
        TransactionRequest request = req(TransactionType.TRANSFER, "A1", "A2", 40);

        Account from = account("A1", 100);
        Account to = account("A2", 10);

        when(accountRepo.findByAccountNumber("A1")).thenReturn(Optional.of(from));
        when(accountRepo.findByAccountNumber("A2")).thenReturn(Optional.of(to));
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction tx = service.execute(request);

        assertEquals(BigDecimal.valueOf(60), from.getBalance());
        assertEquals(BigDecimal.valueOf(50), to.getBalance());
        assertEquals(TransactionType.TRANSFER, tx.getType());
    }

    @Test
    void execute_shouldThrow_whenTransferInsufficientFunds() {
        TransactionRequest request = req(TransactionType.TRANSFER, "A1", "A2", 200);

        Account from = account("A1", 50);
        Account to = account("A2", 10);

        when(accountRepo.findByAccountNumber("A1")).thenReturn(Optional.of(from));
        when(accountRepo.findByAccountNumber("A2")).thenReturn(Optional.of(to));

        assertThrows(InsufficientFundsException.class, () -> service.execute(request));
    }

    @Test
    void execute_shouldThrow_whenTransferSameAccount() {
        TransactionRequest request = req(TransactionType.TRANSFER, "A1", "A1", 10);

        assertThrows(SameAccountTransferException.class, () -> service.execute(request));
    }

    // ---------------------------------------------------------
    // ACCOUNT NOT FOUND
    // ---------------------------------------------------------
    @Test
    void execute_shouldThrow_whenAccountNotFound() {
        TransactionRequest request = req(TransactionType.DEPOSIT, "X", null, 10);

        when(accountRepo.findByAccountNumber("X")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> service.execute(request));
    }
}