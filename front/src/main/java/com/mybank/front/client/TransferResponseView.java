package com.mybank.front.client;

import java.math.BigDecimal;

public record TransferResponseView(
        String status,
        String senderUsername,
        String recipientUsername,
        BigDecimal amount,
        BigDecimal senderBalance,
        BigDecimal recipientBalance
) {
}
