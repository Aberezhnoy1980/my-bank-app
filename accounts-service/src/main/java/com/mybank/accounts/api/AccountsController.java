package com.mybank.accounts.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountsController {

    @GetMapping("/me")
    public AccountProfileResponse getCurrentAccount() {
        return new AccountProfileResponse(
                "demo.user",
                "Demo User",
                LocalDate.of(1995, 5, 20),
                new BigDecimal("10000.00")
        );
    }
}
