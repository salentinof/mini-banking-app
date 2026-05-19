package com.bankapp.mini_banking_app.dto.request;

import com.bankapp.mini_banking_app.entity.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransactionRequest {
    private TransactionType type;
    @NotBlank
    private String accountNumber;
    private String fromAccount;
    private String toAccount;
    @NotNull
    @Positive
    private BigDecimal amount;
}
