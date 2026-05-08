package com.mybank.accounts.service;

import com.mybank.accounts.domain.AccountProfile;
import com.mybank.accounts.api.UpdateAccountProfileRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class AccountProfileService {

    private static final String CURRENT_USERNAME = "demo.user";

    private final Map<String, AccountProfile> accounts = new ConcurrentHashMap<>(Map.of(
            "demo.user", new AccountProfile(
                    "demo.user",
                    "Demo User",
                    LocalDate.of(1995, 5, 20),
                    new BigDecimal("10000.00")
            ),
            "alice.user", new AccountProfile(
                    "alice.user",
                    "Alice User",
                    LocalDate.of(1992, 3, 15),
                    new BigDecimal("5000.00")
            )
    ));

    public AccountProfile getCurrentAccount() {
        return getAccountByUsername(CURRENT_USERNAME);
    }

    public AccountProfile updateCurrentAccount(UpdateAccountProfileRequest request) {
        return updateAccountPersonalData(CURRENT_USERNAME, request);
    }

    public AccountProfile deposit(BigDecimal amount) {
        return depositByUsername(CURRENT_USERNAME, amount);
    }

    public AccountProfile withdraw(BigDecimal amount) {
        return withdrawByUsername(CURRENT_USERNAME, amount);
    }

    public AccountProfile getAccountByUsername(String username) {
        AccountProfile account = accounts.get(username);
        if (account == null) {
            throw new AccountNotFoundException("account not found");
        }
        return account;
    }

    public AccountProfile updateAccountPersonalData(String username, UpdateAccountProfileRequest request) {
        return accounts.compute(username, (key, account) -> {
            if (account == null) {
                throw new AccountNotFoundException("account not found");
            }
            return account.withPersonalData(request.fullName(), request.birthDate());
        });
    }

    public AccountProfile depositByUsername(String username, BigDecimal amount) {
        return accounts.compute(username, (key, account) -> {
            if (account == null) {
                throw new AccountNotFoundException("account not found");
            }
            return account.withBalance(account.balance().add(amount));
        });
    }

    public AccountProfile withdrawByUsername(String username, BigDecimal amount) {
        return accounts.compute(username, (key, account) -> {
            if (account == null) {
                throw new AccountNotFoundException("account not found");
            }
            if (account.balance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("insufficient funds");
            }
            return account.withBalance(account.balance().subtract(amount));
        });
    }
}
