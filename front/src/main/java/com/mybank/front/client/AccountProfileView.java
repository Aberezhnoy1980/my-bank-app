package com.mybank.front.client;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountProfileView(
        String username,
        String fullName,
        LocalDate birthDate,
        BigDecimal balance
) {
}
