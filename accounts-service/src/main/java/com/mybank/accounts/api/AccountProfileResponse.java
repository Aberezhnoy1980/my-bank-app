package com.mybank.accounts.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountProfileResponse(
        String username,
        String fullName,
        LocalDate birthDate,
        BigDecimal balance
) {
}
