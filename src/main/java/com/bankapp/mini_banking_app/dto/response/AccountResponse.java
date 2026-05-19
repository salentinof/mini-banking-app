package com.bankapp.mini_banking_app.dto.response;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String accountNumber,
        BigDecimal balance,
        String currency,
        Long userId
) {}
