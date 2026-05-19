package com.mybank.cash.client;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountProfileView(
        String username,
        String fullName,
        LocalDate birthDate,
        BigDecimal balance
) {
}
