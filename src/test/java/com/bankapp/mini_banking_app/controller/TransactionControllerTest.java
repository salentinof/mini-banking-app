package com.bankapp.mini_banking_app.controller;

import com.bankapp.mini_banking_app.dto.request.DepositRequest;
import com.bankapp.mini_banking_app.dto.request.TransferRequest;
import com.bankapp.mini_banking_app.dto.request.WithdrawRequest;
import com.bankapp.mini_banking_app.dto.response.TransactionResponse;
import com.bankapp.mini_banking_app.entity.Account;
import com.bankapp.mini_banking_app.entity.Transaction;
import com.bankapp.mini_banking_app.entity.TransactionType;
import com.bankapp.mini_banking_app.exception.AccountNotFoundException;
import com.bankapp.mini_banking_app.exception.InsufficientFundsException;
import com.bankapp.mini_banking_app.exception.SameAccountTransferException;
import com.bankapp.mini_banking_app.mapper.TransactionMapper;
import com.bankapp.mini_banking_app.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

    @MockitoBean
    private TransactionService service;

    @MockitoBean
    private TransactionMapper mapper;

    // =========================================
    // POST /transactions/deposit
    // =========================================

    @Test
    void deposit_shouldReturn201_whenRequestIsValid() throws Exception {
        Transaction tx = buildTransaction(1L, TransactionType.DEPOSIT, new BigDecimal("100.00"), "ACC1", null);
        TransactionResponse resp = buildResponse(tx);

        when(service.deposit(any(DepositRequest.class))).thenReturn(tx);
        when(mapper.toResponse(tx)).thenReturn(resp);

        mockMvc.perform(post("/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountNumber": "ACC1",
                                  "amount": 100.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.fromAccountId").value("ACC1"));
    }

    @Test
    void deposit_shouldReturn404_whenAccountNotFound() throws Exception {
        when(service.deposit(any(DepositRequest.class)))
                .thenThrow(new AccountNotFoundException("ACC404"));

        mockMvc.perform(post("/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountNumber": "ACC404",
                                  "amount": 50
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value(new AccountNotFoundException("ACC404").getMessage()));
    }

    @Test
    void deposit_shouldReturn400_whenAmountIsNegative() throws Exception {
        mockMvc.perform(post("/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountNumber": "ACC1",
                                  "amount": -10
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }

    @Test
    void deposit_shouldReturn400_whenAccountNumberIsMissing() throws Exception {
        mockMvc.perform(post("/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 10
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }

    // =========================================
    // POST /transactions/withdraw
    // =========================================

    @Test
    void withdraw_shouldReturn201_whenRequestIsValid() throws Exception {
        Transaction tx = buildTransaction(1L, TransactionType.WITHDRAW, new BigDecimal("100.00"), "ACC1", null);
        TransactionResponse resp = buildResponse(tx);

        when(service.withdraw(any(WithdrawRequest.class))).thenReturn(tx);
        when(mapper.toResponse(tx)).thenReturn(resp);

        mockMvc.perform(post("/transactions/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                  "accountNumber": "ACC1",
                                  "amount": 100.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("WITHDRAW"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.fromAccountId").value("ACC1"));

    }

    @Test
    void withdraw_shouldReturn400_whenInsufficientFunds() throws Exception {
        InsufficientFundsException ex = new InsufficientFundsException("ACC1", new BigDecimal("10"), new BigDecimal("50"));
        when(service.withdraw(any(WithdrawRequest.class))).thenThrow(ex);

        mockMvc.perform(post("/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountNumber": "ACC1",
                                  "amount": 50
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value(ex.getMessage()));
    }

    @Test
    void withdraw_shouldReturn404_whenAccountNotFound() throws Exception {
        when(service.withdraw(any(WithdrawRequest.class)))
                .thenThrow(new AccountNotFoundException("ACC404"));

        mockMvc.perform(post("/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountNumber": "ACC404",
                                  "amount": 10
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void withdraw_shouldReturn400_whenAmountIsMissing() throws Exception {
        mockMvc.perform(post("/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountNumber": "ACC1"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }

    // =========================================
    // POST /transactions/transfer
    // =========================================

    @Test
    void transfer_shouldReturn201_whenRequestIsValid() throws Exception {
        Transaction tx = buildTransaction(1L, TransactionType.TRANSFER, new BigDecimal("20.00"), "ACC1", "ACC2");
        TransactionResponse resp = buildResponse(tx);

        when(service.transfer(any(TransferRequest.class))).thenReturn(tx);
        when(mapper.toResponse(tx)).thenReturn(resp);

        mockMvc.perform(post("/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromAccount": "ACC1",
                                  "toAccount": "ACC2",
                                  "amount": 20.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.amount").value(20.00))
                .andExpect(jsonPath("$.fromAccountId").value("ACC1"))
                .andExpect(jsonPath("$.toAccountId").value("ACC2"));
    }

    @Test
    void transfer_shouldReturn400_whenSameAccount() throws Exception {
        when(service.transfer(any(TransferRequest.class)))
                .thenThrow(new SameAccountTransferException("ACC1"));

        mockMvc.perform(post("/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromAccount": "ACC1",
                                  "toAccount": "ACC1",
                                  "amount": 20
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value(new SameAccountTransferException("ACC1").getMessage()));
    }

    @Test
    void transfer_shouldReturn400_whenInsufficientFunds() throws Exception {
        when(service.transfer(any(TransferRequest.class)))
                .thenThrow(new InsufficientFundsException("ACC1", new BigDecimal("10"), new BigDecimal("50")));

        mockMvc.perform(post("/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromAccount": "ACC1",
                                  "toAccount": "ACC2",
                                  "amount": 50
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void transfer_shouldReturn404_whenDestinationNotFound() throws Exception {
        when(service.transfer(any(TransferRequest.class)))
                .thenThrow(new AccountNotFoundException("ACC404"));

        mockMvc.perform(post("/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromAccount": "ACC1",
                                  "toAccount": "ACC404",
                                  "amount": 10
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void transfer_shouldReturn400_whenToAccountIsMissing() throws Exception {
        mockMvc.perform(post("/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromAccount": "ACC1",
                                  "amount": 10
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }

    // =========================================
    // MALFORMED JSON
    // =========================================

    @Test
    void deposit_shouldReturn400_whenJsonIsMalformed() throws Exception {
        mockMvc.perform(post("/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not valid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed JSON request"));

        verifyNoInteractions(service);
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
