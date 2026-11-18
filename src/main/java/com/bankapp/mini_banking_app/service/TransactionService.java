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
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepo;
    private final TransactionRepository transactionRepo;

    public Transaction deposit(DepositRequest request){

        Transaction tx = new Transaction();
        tx.setType(TransactionType.DEPOSIT);
        tx.setAmount(request.amount());

        Account account = findAccount(request.accountNumber());
        account.setBalance(account.getBalance().add(request.amount()));
        log.info("Deposit completed, account={}, newBalance={}",
                account.getAccountNumber(), account.getBalance());
        tx.setAccount(account);

        tx.setTimestamp(LocalDateTime.now());
        return transactionRepo.save(tx);

    }

    public Transaction withdraw(WithdrawRequest request){

        Transaction tx = new Transaction();
        tx.setType(TransactionType.WITHDRAW);
        tx.setAmount(request.amount());

        Account account = findAccount(request.accountNumber());
        withdraw(account, request.amount());
        log.info("Withdraw completed, account={}, newBalance={}", account.getAccountNumber(), account.getBalance());
        tx.setAccount(account);

        tx.setTimestamp(LocalDateTime.now());
        return transactionRepo.save(tx);
    }

    public Transaction transfer(TransferRequest request) {

        Transaction tx = new Transaction();
        tx.setType(TransactionType.TRANSFER);
        tx.setAmount(request.amount());

        log.info("Transaction start. type={}, amount={}, from={}, to={}",
                tx.getType(),
                tx.getAmount(),
                request.fromAccount(),
                request.toAccount());

        if (Objects.equals(request.fromAccount(), request.toAccount())) {
            throw new SameAccountTransferException(request.toAccount());
        }

        transfer(request, tx);
        log.info("Transfer completed. from={}, to={}, amount={}",
                request.fromAccount(), request.toAccount(), request.amount());

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

    private void transfer(TransferRequest request, Transaction tx) {

        Account from = findAccount(request.fromAccount());
        Account to = findAccount(request.toAccount());

        BigDecimal saldo = from.getBalance().subtract(request.amount());

        if (saldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException(
                    from.getAccountNumber(), from.getBalance(), request.amount());
        }
        from.setBalance(saldo);
        to.setBalance(to.getBalance().add(request.amount()));
        tx.setAccount(from);
        tx.setToAccountId(to.getAccountNumber());
    }

    private Account findAccount(String accountNumber) {
        return accountRepo.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.warn("Account not found: {}", accountNumber);
                    return new AccountNotFoundException(accountNumber);
                });
    }
}
