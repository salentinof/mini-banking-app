package com.bankapp.mini_banking_app.dto.response;

import com.bankapp.mini_banking_app.entity.Account;

import java.util.List;

public record UserResponse(
        Long id,
        String name,
        String email,
        List<String> accountNumbers
){}
