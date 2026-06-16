package com.bankapp.mini_banking_app.exception;

import com.bankapp.mini_banking_app.entity.Transaction;
import com.bankapp.mini_banking_app.entity.TransactionType;

public class InvalidTransactionException extends RuntimeException{

    public InvalidTransactionException(TransactionType type){
        super("Invalid transaction type | type = " + type);
    }
}
