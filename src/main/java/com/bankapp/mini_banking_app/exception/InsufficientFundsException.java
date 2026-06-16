package com.bankapp.mini_banking_app.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String accountNumber, BigDecimal balance, BigDecimal requestAmount) {

        super("Insufficent funds | account = %s | balance = %s | request = %s"
                .formatted(accountNumber, balance, requestAmount));
    }
}
