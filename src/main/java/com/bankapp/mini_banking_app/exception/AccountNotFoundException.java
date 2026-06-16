package com.bankapp.mini_banking_app.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String accountNumber) {

        super("Account not found | accountNumber = " + accountNumber);
    }
}
