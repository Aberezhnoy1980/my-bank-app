package com.mybank.cash.client;

import java.math.BigDecimal;

public record UpdateBalanceRequest(
        BalanceOperationType operationType,
        BigDecimal amount
) {
}
