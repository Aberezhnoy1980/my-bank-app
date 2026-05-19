package com.mybank.front.client;

import java.math.BigDecimal;

public record TransferRequest(
        String recipientUsername,
        BigDecimal amount
) {
}
