package com.mybank.accounts.api;

import com.mybank.accounts.validation.Adult;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record UpdateAccountProfileRequest(
        @NotBlank(message = "fullName must not be blank")
        String fullName,
        @Adult
        LocalDate birthDate
) {
}
