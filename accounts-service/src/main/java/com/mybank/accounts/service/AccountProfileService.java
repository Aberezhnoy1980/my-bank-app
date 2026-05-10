package com.mybank.accounts.service;

import com.mybank.accounts.api.UpdateAccountProfileRequest;
import com.mybank.accounts.domain.AccountProfile;
import com.mybank.accounts.persistence.AccountEntity;
import com.mybank.accounts.persistence.AccountRepository;
import java.math.BigDecimal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountProfileService {

    private static final String FALLBACK_USERNAME = "demo.user";

    private final AccountRepository accountRepository;

    public AccountProfileService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public AccountProfile getCurrentAccount() {
        return getAccountByUsername(resolveCurrentUsername());
    }

    @Transactional
    public AccountProfile updateCurrentAccount(UpdateAccountProfileRequest request) {
        return updateAccountPersonalData(resolveCurrentUsername(), request);
    }

    @Transactional
    public AccountProfile deposit(BigDecimal amount) {
        return depositByUsername(resolveCurrentUsername(), amount);
    }

    @Transactional
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

    @Transactional(readOnly = true)
    public AccountProfile getAccountByUsername(String username) {
        AccountEntity entity = accountRepository.findById(username)
                .orElseThrow(() -> new AccountNotFoundException("account not found"));
        return toDomain(entity);
    }

    @Transactional
    public AccountProfile updateAccountPersonalData(String username, UpdateAccountProfileRequest request) {
        AccountEntity entity = accountRepository.findById(username)
                .orElseThrow(() -> new AccountNotFoundException("account not found"));
        entity.setFullName(request.fullName());
        entity.setBirthDate(request.birthDate());
        accountRepository.save(entity);
        return toDomain(entity);
    }

    @Transactional
    public AccountProfile depositByUsername(String username, BigDecimal amount) {
        AccountEntity entity = accountRepository.findById(username)
                .orElseThrow(() -> new AccountNotFoundException("account not found"));
        entity.setBalance(entity.getBalance().add(amount));
        accountRepository.save(entity);
        return toDomain(entity);
    }

    @Transactional
    public AccountProfile withdrawByUsername(String username, BigDecimal amount) {
        AccountEntity entity = accountRepository.findById(username)
                .orElseThrow(() -> new AccountNotFoundException("account not found"));
        if (entity.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("insufficient funds");
        }
        entity.setBalance(entity.getBalance().subtract(amount));
        accountRepository.save(entity);
        return toDomain(entity);
    }

    private static AccountProfile toDomain(AccountEntity entity) {
        return new AccountProfile(
                entity.getUsername(),
                entity.getFullName(),
                entity.getBirthDate(),
                entity.getBalance()
        );
    }
}
