package com.bankapp.mini_banking_app.dto.request;

public record AuthRequest(
        String username,
        String password
) {}