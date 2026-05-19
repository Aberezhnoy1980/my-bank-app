package com.mybank.transfer.api;

import java.math.BigDecimal;

public record TransferResponse(
        String status,
        String senderUsername,
        String recipientUsername,
        BigDecimal amount,
        BigDecimal senderBalance,
        BigDecimal recipientBalance
) {
}
