package com.bankapp.mini_banking_app.controller;

import com.bankapp.mini_banking_app.dto.request.TransactionRequest;
import com.bankapp.mini_banking_app.entity.Transaction;
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

    @PostMapping
    public ResponseEntity<Transaction> addTransaction(@Valid @RequestBody TransactionRequest request){
       return ResponseEntity
               .status(HttpStatus.CREATED)
               .body(service.execute(request));
    }
}
