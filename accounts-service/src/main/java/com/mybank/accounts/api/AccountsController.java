package com.mybank.accounts.api;

import com.mybank.accounts.kafka.NotificationEventPublisher;
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
    private final NotificationEventPublisher notificationEventPublisher;

    public AccountsController(
            AccountProfileService accountProfileService,
            NotificationEventPublisher notificationEventPublisher
    ) {
        this.accountProfileService = accountProfileService;
        this.notificationEventPublisher = notificationEventPublisher;
    }

    @GetMapping("/me")
    public AccountProfileResponse getCurrentAccount() {
        return toResponse(accountProfileService.getCurrentAccount());
    }

    @PutMapping("/me")
    public AccountProfileResponse updateCurrentAccount(@Valid @RequestBody UpdateAccountProfileRequest request) {
        AccountProfileResponse response = toResponse(accountProfileService.updateCurrentAccount(request));
        notificationEventPublisher.send("ACCOUNT_PROFILE_UPDATED", "Profile updated for " + response.username());
        return response;
    }

    @PutMapping("/me/balance")
    public AccountProfileResponse updateCurrentAccountBalance(@Valid @RequestBody UpdateBalanceRequest request) {
        AccountProfileResponse response;
        if (request.operationType() == BalanceOperationType.DEPOSIT) {
            response = toResponse(accountProfileService.deposit(request.amount()));
        } else {
            response = toResponse(accountProfileService.withdraw(request.amount()));
        }
        notificationEventPublisher.send("ACCOUNT_BALANCE_UPDATED", "Balance updated for " + response.username());
        return response;
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
        AccountProfileResponse response;
        if (request.operationType() == BalanceOperationType.DEPOSIT) {
            response = toResponse(accountProfileService.depositByUsername(username, request.amount()));
        } else {
            response = toResponse(accountProfileService.withdrawByUsername(username, request.amount()));
        }
        notificationEventPublisher.send("ACCOUNT_BALANCE_UPDATED", "Balance updated for " + response.username());
        return response;
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
