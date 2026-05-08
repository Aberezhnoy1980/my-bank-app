package com.mybank.accounts.service;

import com.mybank.accounts.domain.AccountProfile;
import com.mybank.accounts.api.UpdateAccountProfileRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;

@Service
public class AccountProfileService {

    private final AtomicReference<AccountProfile> currentAccount = new AtomicReference<>(
            new AccountProfile(
                    "demo.user",
                    "Demo User",
                    LocalDate.of(1995, 5, 20),
                    new BigDecimal("10000.00")
            )
    );

    public AccountProfile getCurrentAccount() {
        return currentAccount.get();
    }

    public AccountProfile updateCurrentAccount(UpdateAccountProfileRequest request) {
        return currentAccount.updateAndGet(account ->
                account.withPersonalData(request.fullName(), request.birthDate()));
    }

    public AccountProfile deposit(BigDecimal amount) {
        return currentAccount.updateAndGet(account ->
                account.withBalance(account.balance().add(amount)));
    }

    public AccountProfile withdraw(BigDecimal amount) {
        return currentAccount.updateAndGet(account -> {
            if (account.balance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("insufficient funds");
            }
            return account.withBalance(account.balance().subtract(amount));
        });
    }
}
