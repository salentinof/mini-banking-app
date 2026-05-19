package com.bankapp.mini_banking_app.service;

import com.bankapp.mini_banking_app.dto.request.TransactionRequest;
import com.bankapp.mini_banking_app.entity.Account;
import com.bankapp.mini_banking_app.entity.Transaction;
import com.bankapp.mini_banking_app.exception.AccountNotFoundException;
import com.bankapp.mini_banking_app.exception.InsufficientFundsException;
import com.bankapp.mini_banking_app.exception.InvalidTransactionException;
import com.bankapp.mini_banking_app.exception.SameAccountTransferException;
import com.bankapp.mini_banking_app.repository.AccountRepository;
import com.bankapp.mini_banking_app.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Log4j2
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepo;
    private final TransactionRepository transactionRepo;


    @Transactional
    public Transaction execute(TransactionRequest request) {

        Transaction tx = new Transaction();
        tx.setType(request.getType());
        tx.setAmount(request.getAmount());

        log.info("Transaction start. type={}, amount={},account={}, from={}, to={}",
                tx.getType(),
                tx.getAmount(),
                request.getAccountNumber(),
                request.getFromAccount(),
                request.getToAccount());

        switch (request.getType()) {

            case DEPOSIT -> {
                Account account = findAccount(request.getAccountNumber());
                account.setBalance(account.getBalance().add(request.getAmount()));
                log.info("Deposit completed, account={}, newBalance={}",
                        account.getAccountNumber(), account.getBalance());
                tx.setAccount(account);
            }
            case WITHDRAW -> {
                Account account = findAccount(request.getAccountNumber());
                withdraw(account, request.getAmount());
                log.info("Withdraw completed, account={}, newBalance={}", account.getAccountNumber(), account.getBalance());
                tx.setAccount(account);
            }
            case TRANSFER -> {

                if (Objects.equals(request.getFromAccount(), request.getToAccount())) {
                    throw new SameAccountTransferException(request.getToAccount());
                }

                transfer(request, tx);
                log.info("Transfer completed. from={}, to={}, amount={}",
                        request.getFromAccount(), request.getToAccount(), request.getAmount());
            }
            default -> throw new InvalidTransactionException(request.getType());
        }


        tx.setTimestamp(LocalDateTime.now());
        return transactionRepo.save(tx);
    }

    private void withdraw(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    account.getAccountNumber(),
                    account.getBalance(),
                    amount
            );
        }

        account.setBalance(account.getBalance().subtract(amount));
    }

    private void transfer(TransactionRequest request, Transaction tx) {

        Account from = findAccount(request.getFromAccount());
        Account to = findAccount(request.getToAccount());

        BigDecimal saldo = from.getBalance().subtract(request.getAmount());

        if (saldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException(
                    from.getAccountNumber(), from.getBalance(), request.getAmount());
        }
        from.setBalance(saldo);
        to.setBalance(to.getBalance().add(request.getAmount()));
        tx.setAccount(from);
    }

    private Account findAccount(String accountNumber) {
        return accountRepo.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.warn("Account not found: {}", accountNumber);
                    return new AccountNotFoundException(accountNumber);
                });
    }
}
