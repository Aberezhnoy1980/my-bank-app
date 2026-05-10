package com.mybank.cash.service;

import com.mybank.cash.api.CashOperationResponse;
import com.mybank.cash.client.AccountProfileView;
import com.mybank.cash.client.AccountsClient;
import com.mybank.cash.client.NotificationsClient;
import com.mybank.cash.persistence.CashOperationEntity;
import com.mybank.cash.persistence.CashOperationRepository;
import com.mybank.cash.persistence.CashOperationType;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class CashService {

    private final AccountsClient accountsClient;
    private final NotificationsClient notificationsClient;
    private final CashOperationRepository cashOperationRepository;
    private final String fallbackUsername;

    public CashService(
            AccountsClient accountsClient,
            NotificationsClient notificationsClient,
            CashOperationRepository cashOperationRepository,
            @Value("${app.cash.fallback-username}") String fallbackUsername
    ) {
        this.accountsClient = accountsClient;
        this.notificationsClient = notificationsClient;
        this.cashOperationRepository = cashOperationRepository;
        this.fallbackUsername = fallbackUsername;
    }

    public CashOperationResponse deposit(BigDecimal amount) {
        String username = resolveUsername();
        AccountProfileView profile = accountsClient.deposit(amount);
        cashOperationRepository.save(new CashOperationEntity(username, CashOperationType.DEPOSIT, amount));
        notificationsClient.send("CASH_DEPOSIT", "Deposit completed for " + username + " amount " + amount);
        return new CashOperationResponse("DEPOSIT_SUCCESS", profile.balance());
    }

    public CashOperationResponse withdraw(BigDecimal amount) {
        String username = resolveUsername();
        AccountProfileView profile = accountsClient.withdraw(amount);
        cashOperationRepository.save(new CashOperationEntity(username, CashOperationType.WITHDRAW, amount));
        notificationsClient.send("CASH_WITHDRAW", "Withdraw completed for " + username + " amount " + amount);
        return new CashOperationResponse("WITHDRAW_SUCCESS", profile.balance());
    }

    private String resolveUsername() {
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
        return fallbackUsername;
    }
}
