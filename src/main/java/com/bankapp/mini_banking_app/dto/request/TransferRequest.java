package com.bankapp.mini_banking_app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank String fromAccount,
        @NotBlank String toAccount,
        @NotNull @Positive BigDecimal amount
) {}
