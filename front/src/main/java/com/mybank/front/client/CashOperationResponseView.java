package com.mybank.front.client;

import java.math.BigDecimal;

public record CashOperationResponseView(
        String status,
        BigDecimal balance
) {
}
