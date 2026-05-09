package com.mybank.accounts.service;

import com.mybank.accounts.domain.AccountProfile;
import com.mybank.accounts.api.UpdateAccountProfileRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AccountProfileService {

    private static final String FALLBACK_USERNAME = "demo.user";

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
        return getAccountByUsername(resolveCurrentUsername());
    }

    public AccountProfile updateCurrentAccount(UpdateAccountProfileRequest request) {
        return updateAccountPersonalData(resolveCurrentUsername(), request);
    }

    public AccountProfile deposit(BigDecimal amount) {
        return depositByUsername(resolveCurrentUsername(), amount);
    }

    public AccountProfile withdraw(BigDecimal amount) {
        return withdrawByUsername(resolveCurrentUsername(), amount);
    }

    private String resolveCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            String preferred = jwt.getToken().getClaimAsString("preferred_username");
            if (preferred != null && !preferred.isBlank()) {
                return preferred;
            }
            String sub = jwt.getToken().getSubject();
            if (sub != null && !sub.isBlank()) {
                return sub;
            }
        }
        return FALLBACK_USERNAME;
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
