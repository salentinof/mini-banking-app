package com.bankapp.mini_banking_app.controller;


import com.bankapp.mini_banking_app.dto.request.AccountRequest;
import com.bankapp.mini_banking_app.dto.response.AccountResponse;
import com.bankapp.mini_banking_app.entity.Account;
import com.bankapp.mini_banking_app.entity.User;
import com.bankapp.mini_banking_app.exception.AccountNotFoundException;
import com.bankapp.mini_banking_app.mapper.AccountMapper;
import com.bankapp.mini_banking_app.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private AccountMapper accountMapper;


    // ============================
    // CREATE ACCOUNT - SUCCESS
    // ============================
    @Test
    void createAccount_success() throws Exception {
        Account acc = buildAccount(1L, "ACC123", new BigDecimal("100.00"), "EUR", 10L);
        AccountResponse resp = buildResponse(acc);

        when(accountService.createAccount(any(AccountRequest.class))).thenReturn(acc);
        when(accountMapper.responseDto(any(Account.class))).thenReturn(resp);

        mockMvc.perform(
                        post("/accounts")
                                .contentType(MediaType.APPLICATION_JSON.toString())
                                .content("""
                                        {"userId":10}
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountNumber").value("ACC123"))
                .andExpect(jsonPath("$.balance").value(100.00))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.userId").value(10));
    }

    // ============================
    // GET ACCOUNT - SUCCESS
    // ============================
    @Test
    void getAccount_success() throws Exception {
        Account acc = buildAccount(1L, "ACC999", new BigDecimal("500.00"), "EUR", 20L);
        AccountResponse resp = buildResponse(acc);

        when(accountService.getAccount(anyString())).thenReturn(acc);
        when(accountMapper.responseDto(any(Account.class))).thenReturn(resp);

        mockMvc.perform(get("/accounts/ACC999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountNumber").value("ACC999"))
                .andExpect(jsonPath("$.balance").value(500.00))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.userId").value(20));
    }

    // ============================
    // GET ACCOUNT - NOT FOUND
    // ============================
    @Test
    void getAccount_notFound() throws Exception {
        when(accountService.getAccount(anyString()))
                .thenThrow(new AccountNotFoundException("ACC404"));

        mockMvc.perform(get("/accounts/ACC404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value(new AccountNotFoundException("ACC404").getMessage()));
    }

    // ============================
    // GET ALL ACCOUNTS - SUCCESS
    // ============================
    @Test
    void getAllAccounts_success() throws Exception {
        Account a1 = buildAccount(1L, "ACC1", new BigDecimal("100.00"), "EUR", 10L);
        Account a2 = buildAccount(2L, "ACC2", new BigDecimal("200.00"), "EUR", 20L);

        AccountResponse r1 = buildResponse(a1);
        AccountResponse r2 = buildResponse(a2);

        when(accountService.getAll()).thenReturn(List.of(a1, a2));
        when(accountMapper.responseDto(anyList())).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].accountNumber").value("ACC1"))
                .andExpect(jsonPath("$[0].balance").value(100.00))
                .andExpect(jsonPath("$[0].currency").value("EUR"))
                .andExpect(jsonPath("$[0].userId").value(10))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].accountNumber").value("ACC2"))
                .andExpect(jsonPath("$[1].balance").value(200.00))
                .andExpect(jsonPath("$[1].currency").value("EUR"))
                .andExpect(jsonPath("$[1].userId").value(20));
    }

    // ============================
    // HELPERS
    // ============================
    private Account buildAccount(Long id, String number, BigDecimal balance, String currency, Long userId) {
        User u = new User();
        u.setId(userId);

        Account a = new Account();
        a.setId(id);
        a.setAccountNumber(number);
        a.setBalance(balance);
        a.setCurrency(currency);
        a.setUser(u);

        return a;
    }

    private AccountResponse buildResponse(Account a) {
        return new AccountResponse(
                a.getId(),
                a.getAccountNumber(),
                a.getBalance(),
                a.getCurrency(),
                a.getUser().getId()
        );
    }
}
