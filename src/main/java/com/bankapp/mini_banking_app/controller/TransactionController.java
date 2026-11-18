package com.bankapp.mini_banking_app.controller;

import com.bankapp.mini_banking_app.dto.request.DepositRequest;
import com.bankapp.mini_banking_app.dto.request.TransferRequest;
import com.bankapp.mini_banking_app.dto.request.WithdrawRequest;
import com.bankapp.mini_banking_app.dto.response.TransactionResponse;
import com.bankapp.mini_banking_app.entity.Transaction;
import com.bankapp.mini_banking_app.mapper.TransactionMapper;
import com.bankapp.mini_banking_app.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;
    private final TransactionMapper transactionMapper;


    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> depositTransaction(@Valid @RequestBody DepositRequest request){

        Transaction tx = service.deposit(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionMapper.toResponse(tx));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdrawTransaction(@Valid @RequestBody WithdrawRequest request){
        Transaction tx = service.withdraw(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionMapper.toResponse(tx));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transferTransaction(@Valid @RequestBody TransferRequest request){
        Transaction tx = service.transfer(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionMapper.toResponse(tx));
    }
}
