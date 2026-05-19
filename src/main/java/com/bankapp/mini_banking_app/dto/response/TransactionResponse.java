package com.bankapp.mini_banking_app.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String type,
        BigDecimal amount,
        LocalDateTime timestamp,
        String fromAccountId,
        String toAccountId
) {}
