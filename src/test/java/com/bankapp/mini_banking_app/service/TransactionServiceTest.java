package com.bankapp.mini_banking_app.service;

import com.bankapp.mini_banking_app.dto.request.DepositRequest;
import com.bankapp.mini_banking_app.dto.request.TransferRequest;
import com.bankapp.mini_banking_app.dto.request.WithdrawRequest;
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
    // Helpers
    // ---------------------------------------------------------

    private Account account(String number, int balance) {
        Account acc = new Account();
        acc.setAccountNumber(number);
        acc.setBalance(BigDecimal.valueOf(balance));
        return acc;
    }

    private BigDecimal eur(int amount) {
        return BigDecimal.valueOf(amount);
    }

    private void givenAccounts(Account... accounts) {
        for (Account acc : accounts) {
            when(accountRepo.findByAccountNumber(acc.getAccountNumber())).thenReturn(Optional.of(acc));
        }
    }

    private void givenSaveReturnsArgument() {
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ---------------------------------------------------------
    // DEPOSIT
    // ---------------------------------------------------------

    @Test
    void deposit_shouldAddAmountAndSaveTransaction() {
        Account acc = account("A1", 50);
        givenAccounts(acc);
        givenSaveReturnsArgument();

        Transaction tx = service.deposit(new DepositRequest("A1", eur(100)));

        assertEquals(eur(150), acc.getBalance());
        assertEquals(TransactionType.DEPOSIT, tx.getType());
        assertEquals(eur(100), tx.getAmount());
        assertSame(acc, tx.getAccount());
        assertNotNull(tx.getTimestamp());
        assertNull(tx.getToAccountId());
        assertEquals(eur(150), tx.getBalanceAfter());
        verify(transactionRepo).save(tx);
    }

    @Test
    void deposit_shouldThrow_whenAccountNotFound() {
        when(accountRepo.findByAccountNumber("X")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> service.deposit(new DepositRequest("X", eur(10))));

        verify(transactionRepo, never()).save(any());
    }

    // ---------------------------------------------------------
    // WITHDRAW
    // ---------------------------------------------------------

    @Test
    void withdraw_shouldSubtractAmountAndSaveTransaction() {
        Account acc = account("A1", 100);
        givenAccounts(acc);
        givenSaveReturnsArgument();

        Transaction tx = service.withdraw(new WithdrawRequest("A1", eur(30)));

        assertEquals(eur(70), acc.getBalance());
        assertEquals(TransactionType.WITHDRAW, tx.getType());
        assertEquals(eur(70), tx.getBalanceAfter());
        assertSame(acc, tx.getAccount());
        assertNotNull(tx.getTimestamp());
        verify(transactionRepo).save(tx);
    }

    @Test
    void withdraw_shouldAllowWithdrawingEntireBalance() {
        Account acc = account("A1", 50);
        givenAccounts(acc);
        givenSaveReturnsArgument();

        service.withdraw(new WithdrawRequest("A1", eur(50)));

        assertEquals(0, acc.getBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    void withdraw_shouldThrow_whenInsufficientFunds() {
        Account acc = account("A1", 50);
        givenAccounts(acc);

        assertThrows(InsufficientFundsException.class,
                () -> service.withdraw(new WithdrawRequest("A1", eur(200))));

        assertEquals(eur(50), acc.getBalance());
        verify(transactionRepo, never()).save(any());
    }

    @Test
    void withdraw_shouldThrow_whenAccountNotFound() {
        when(accountRepo.findByAccountNumber("X")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> service.withdraw(new WithdrawRequest("X", eur(10))));

        verify(transactionRepo, never()).save(any());
    }

    // ---------------------------------------------------------
    // TRANSFER
    // ---------------------------------------------------------

    @Test
    void transfer_shouldMoveMoneyAndRecordDestination() {
        Account from = account("A1", 100);
        Account to = account("A2", 10);
        givenAccounts(from, to);
        givenSaveReturnsArgument();

        Transaction tx = service.transfer(new TransferRequest("A1", "A2", eur(40)));

        assertEquals(eur(60), from.getBalance());
        assertEquals(eur(50), to.getBalance());
        assertEquals(TransactionType.TRANSFER, tx.getType());
        assertSame(from, tx.getAccount());
        assertEquals("A2", tx.getToAccountId());
        assertEquals(eur(60), tx.getBalanceAfter());
        assertNotNull(tx.getTimestamp());
        verify(transactionRepo).save(tx);
    }

    @Test
    void transfer_shouldThrow_whenInsufficientFunds() {
        Account from = account("A1", 50);
        Account to = account("A2", 10);
        givenAccounts(from, to);

        assertThrows(InsufficientFundsException.class,
                () -> service.transfer(new TransferRequest("A1", "A2", eur(200))));

        assertEquals(eur(50), from.getBalance());
        assertEquals(eur(10), to.getBalance());
        verify(transactionRepo, never()).save(any());
    }

    @Test
    void transfer_shouldThrow_whenSameAccount() {
        assertThrows(SameAccountTransferException.class,
                () -> service.transfer(new TransferRequest("A1", "A1", eur(10))));

        verifyNoInteractions(accountRepo, transactionRepo);
    }

    @Test
    void transfer_shouldThrow_whenDestinationNotFound() {
        Account from = account("A1", 100);
        givenAccounts(from);
        when(accountRepo.findByAccountNumber("X")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> service.transfer(new TransferRequest("A1", "X", eur(10))));

        assertEquals(eur(100), from.getBalance());
        verify(transactionRepo, never()).save(any());
    }
}
