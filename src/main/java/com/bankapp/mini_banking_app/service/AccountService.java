package com.bankapp.mini_banking_app.service;

import com.bankapp.mini_banking_app.dto.request.AccountRequest;
import com.bankapp.mini_banking_app.entity.Account;
import com.bankapp.mini_banking_app.entity.User;
import com.bankapp.mini_banking_app.exception.AccountNotFoundException;
import com.bankapp.mini_banking_app.repository.AccountRepository;
import com.bankapp.mini_banking_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    public Account createAccount(AccountRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(()-> new RuntimeException("user not found"));

        Account account = new Account();
        account.setAccountNumber("ACC-" + System.currentTimeMillis());
        account.setUser(user);
        account.setBalance(BigDecimal.ZERO);

        Account saved= accountRepository.save(account);
        log.info("Account created | userId = {} | accountNumber = {}",
                request.getUserId(),
                account.getAccountNumber());

        return saved;
    }

    public Account getAccount(String accountNumber){
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()-> new AccountNotFoundException(accountNumber));
    }

    public List<Account> getAll(){
        return accountRepository.findAll();
    }

}
