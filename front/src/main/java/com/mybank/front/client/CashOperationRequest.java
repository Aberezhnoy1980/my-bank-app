package com.mybank.front.client;

import java.math.BigDecimal;

public record CashOperationRequest(
        BigDecimal amount
) {
}
