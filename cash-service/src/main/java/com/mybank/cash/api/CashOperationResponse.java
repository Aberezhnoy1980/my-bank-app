package com.mybank.cash.api;

import java.math.BigDecimal;

public record CashOperationResponse(
        String status,
        BigDecimal balance
) {
}
