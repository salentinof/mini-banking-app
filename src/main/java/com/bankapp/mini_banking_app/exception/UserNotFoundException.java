package com.bankapp.mini_banking_app.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User not found | id = " + id);
    }
}

