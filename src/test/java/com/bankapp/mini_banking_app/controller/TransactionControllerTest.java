package com.bankapp.mini_banking_app.controller;

import com.bankapp.mini_banking_app.dto.request.TransactionRequest;
import com.bankapp.mini_banking_app.dto.response.TransactionResponse;
import com.bankapp.mini_banking_app.entity.Account;
import com.bankapp.mini_banking_app.entity.Transaction;
import com.bankapp.mini_banking_app.entity.TransactionType;
import com.bankapp.mini_banking_app.exception.AccountNotFoundException;
import com.bankapp.mini_banking_app.exception.InsufficientFundsException;
import com.bankapp.mini_banking_app.exception.InvalidTransactionException;
import com.bankapp.mini_banking_app.exception.SameAccountTransferException;
import com.bankapp.mini_banking_app.mapper.TransactionMapper;
import com.bankapp.mini_banking_app.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService service;

    @MockBean
    private TransactionMapper mapper;

    // ============================
    // POST /transactions - SUCCESS
    // ============================
    @Test
    void addTransaction_success() throws Exception {
        Transaction tx = buildTransaction(
                1L,
                TransactionType.DEPOSIT,
                new BigDecimal("100.00"),
                "ACC1",
                null
        );

        TransactionResponse resp = buildResponse(tx);

        when(service.execute(any(TransactionRequest.class))).thenReturn(tx);
        when(mapper.toResponse(tx)).thenReturn(resp);

        mockMvc.perform(
                        post("/transactions")
                                .contentType(MediaType.APPLICATION_JSON.toString())
                                .content("""
                                {
                                  "type": "DEPOSIT",
                                  "accountNumber": "ACC1",
                                  "amount": 100.00
                                }
                                """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.fromAccountId").value("ACC1"));
    }

    // =========================================
    // POST /transactions - ACCOUNT NOT FOUND 404
    // =========================================
    @Test
    void addTransaction_accountNotFound() throws Exception {
        when(service.execute(any(TransactionRequest.class)))
                .thenThrow(new AccountNotFoundException("ACC404"));

        mockMvc.perform(
                        post("/transactions")
                                .contentType(MediaType.APPLICATION_JSON.toString())
                                .content("""
                                {
                                  "type": "DEPOSIT",
                                  "accountNumber": "ACC404",
                                  "amount": 50
                                }
                                """)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error")
                        .value(new AccountNotFoundException("ACC404").getMessage()));
    }

    // =========================================
    // POST /transactions - INSUFFICIENT FUNDS 400
    // =========================================
    @Test
    void addTransaction_insufficientFunds() throws Exception {
        when(service.execute(any(TransactionRequest.class)))
                .thenThrow(new InsufficientFundsException("ACC1", new BigDecimal("10"), new BigDecimal("50")));

        mockMvc.perform(
                        post("/transactions")
                                .contentType(MediaType.APPLICATION_JSON.toString())
                                .content("""
                                {
                                  "type": "WITHDRAW",
                                  "accountNumber": "ACC1",
                                  "amount": 50
                                }
                                """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").exists());
    }

    // =========================================
    // POST /transactions - SAME ACCOUNT TRANSFER 400
    // =========================================
    @Test
    void addTransaction_sameAccountTransfer() throws Exception {
        when(service.execute(any(TransactionRequest.class)))
                .thenThrow(new SameAccountTransferException("ACC1"));

        mockMvc.perform(
                        post("/transactions")
                                .contentType(MediaType.APPLICATION_JSON.toString())
                                .content("""
                                {
                                  "type": "TRANSFER",
                                  "accountNumber": "ACC1"
                                  "fromAccount": "ACC1",
                                  "toAccount": "ACC1",
                                  "amount": 20
                                }
                                """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").exists());
    }

    // =========================================
    // POST /transactions - INVALID TYPE 400
    // =========================================
    @Test
    void addTransaction_invalidType() throws Exception {
        when(service.execute(any(TransactionRequest.class)))
                .thenThrow(new InvalidTransactionException(null));

        mockMvc.perform(
                        post("/transactions")
                                .contentType(MediaType.APPLICATION_JSON.toString())
                                .content("""
                                {
                                  "type": "INVALID",
                                  "accountNumber": "ACC1",
                                  "amount": 10
                                }
                                """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").exists());
    }

    // ============================
    // HELPERS
    // ============================
    private Transaction buildTransaction(Long id,
                                         TransactionType type,
                                         BigDecimal amount,
                                         String fromAccount,
                                         String toAccount) {

        Account acc = new Account();
        acc.setAccountNumber(fromAccount);

        Transaction tx = new Transaction();
        tx.setId(id);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setTimestamp(LocalDateTime.now());
        tx.setAccount(acc);
        tx.setToAccountId(toAccount);

        return tx;
    }

    private TransactionResponse buildResponse(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getType().name(),
                tx.getAmount(),
                tx.getTimestamp(),
                tx.getAccount().getAccountNumber(),
                tx.getToAccountId()
        );
    }
}