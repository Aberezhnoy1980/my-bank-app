package com.mybank.accounts.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountProfile(
        String username,
        String fullName,
        LocalDate birthDate,
        BigDecimal balance
) {
    public AccountProfile withPersonalData(String newFullName, LocalDate newBirthDate) {
        return new AccountProfile(username, newFullName, newBirthDate, balance);
    }

    public AccountProfile withBalance(BigDecimal newBalance) {
        return new AccountProfile(username, fullName, birthDate, newBalance);
    }
}
