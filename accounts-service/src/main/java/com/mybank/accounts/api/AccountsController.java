package com.mybank.accounts.api;

import com.mybank.accounts.domain.AccountProfile;
import com.mybank.accounts.service.AccountProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountsController {

    private final AccountProfileService accountProfileService;

    public AccountsController(AccountProfileService accountProfileService) {
        this.accountProfileService = accountProfileService;
    }

    @GetMapping("/me")
    public AccountProfileResponse getCurrentAccount() {
        return toResponse(accountProfileService.getCurrentAccount());
    }

    @PutMapping("/me")
    public AccountProfileResponse updateCurrentAccount(@Valid @RequestBody UpdateAccountProfileRequest request) {
        return toResponse(accountProfileService.updateCurrentAccount(request));
    }

    @PutMapping("/me/balance")
    public AccountProfileResponse updateCurrentAccountBalance(@Valid @RequestBody UpdateBalanceRequest request) {
        if (request.operationType() == BalanceOperationType.DEPOSIT) {
            return toResponse(accountProfileService.deposit(request.amount()));
        }
        return toResponse(accountProfileService.withdraw(request.amount()));
    }

    @GetMapping("/{username}")
    public AccountProfileResponse getAccountByUsername(@PathVariable("username") String username) {
        return toResponse(accountProfileService.getAccountByUsername(username));
    }

    @PutMapping("/{username}/balance")
    public AccountProfileResponse updateAccountBalanceByUsername(
            @PathVariable("username") String username,
            @Valid @RequestBody UpdateBalanceRequest request
    ) {
        if (request.operationType() == BalanceOperationType.DEPOSIT) {
            return toResponse(accountProfileService.depositByUsername(username, request.amount()));
        }
        return toResponse(accountProfileService.withdrawByUsername(username, request.amount()));
    }

    private AccountProfileResponse toResponse(AccountProfile account) {
        return new AccountProfileResponse(
                account.username(),
                account.fullName(),
                account.birthDate(),
                account.balance()
        );
    }
}
