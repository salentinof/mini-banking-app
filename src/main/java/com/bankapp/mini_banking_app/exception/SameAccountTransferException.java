package com.bankapp.mini_banking_app.exception;

public class SameAccountTransferException extends RuntimeException {
    public SameAccountTransferException(String accountNumber) {

        super("Transfer not allowed | fromAccount = %s | toAccount = %s"
                .formatted(accountNumber, accountNumber));
    }
}
