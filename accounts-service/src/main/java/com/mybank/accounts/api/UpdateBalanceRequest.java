package com.mybank.accounts.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateBalanceRequest(
        @NotNull(message = "operationType must be provided")
        BalanceOperationType operationType,
        @NotNull(message = "amount must be provided")
        @DecimalMin(value = "0.01", message = "amount must be greater than 0")
        BigDecimal amount
) {
}
