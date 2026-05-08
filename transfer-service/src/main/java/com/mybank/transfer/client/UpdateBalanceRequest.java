package com.mybank.transfer.client;

import java.math.BigDecimal;

public record UpdateBalanceRequest(
        BalanceOperationType operationType,
        BigDecimal amount
) {
}
