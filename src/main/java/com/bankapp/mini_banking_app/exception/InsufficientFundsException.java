package com.bankapp.mini_banking_app.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String accountNumber, BigDecimal balance, BigDecimal requestAmount) {

        super("Insufficient funds | account = %s | balance = %s | request = %s"
                .formatted(accountNumber, balance, requestAmount));
    }
}
