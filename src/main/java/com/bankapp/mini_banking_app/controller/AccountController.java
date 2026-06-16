package com.bankapp.mini_banking_app.controller;

import com.bankapp.mini_banking_app.dto.request.AccountRequest;
import com.bankapp.mini_banking_app.dto.response.AccountResponse;
import com.bankapp.mini_banking_app.entity.Account;
import com.bankapp.mini_banking_app.mapper.AccountMapper;
import com.bankapp.mini_banking_app.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountRequest request) {
        Account account = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountMapper.responseDto(account));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        Account account = accountService.getAccount(accountNumber);
        return ResponseEntity.ok(accountMapper.responseDto(account));
    }

    @GetMapping()
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        List<Account> list = accountService.getAll();
        List<AccountResponse> listResponse = accountMapper.responseDto(list);
        return ResponseEntity.ok(listResponse);
    }
}
