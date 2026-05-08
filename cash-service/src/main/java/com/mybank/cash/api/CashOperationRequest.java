package com.mybank.cash.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CashOperationRequest(
        @NotNull(message = "amount must be provided")
        @DecimalMin(value = "0.01", message = "amount must be greater than 0")
        BigDecimal amount
) {
}
