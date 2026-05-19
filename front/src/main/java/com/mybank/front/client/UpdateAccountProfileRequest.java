package com.mybank.front.client;

import java.time.LocalDate;

public record UpdateAccountProfileRequest(
        String fullName,
        LocalDate birthDate
) {
}
