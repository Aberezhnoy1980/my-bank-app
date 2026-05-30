package com.mybank.cash.service;

import com.mybank.cash.api.CashOperationResponse;
import com.mybank.cash.client.AccountProfileView;
import com.mybank.cash.client.AccountsClient;
import com.mybank.cash.kafka.NotificationEventPublisher;
import com.mybank.cash.persistence.CashOperationEntity;
import com.mybank.cash.persistence.CashOperationRepository;
import com.mybank.cash.persistence.CashOperationType;
import com.mybank.observability.BusinessMetrics;
import com.mybank.security.support.JwtUsernameResolver;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CashService {

    private final AccountsClient accountsClient;
    private final NotificationEventPublisher notificationEventPublisher;
    private final CashOperationRepository cashOperationRepository;
    private final JwtUsernameResolver jwtUsernameResolver;
    private final BusinessMetrics businessMetrics;
    private final String fallbackUsername;

    public CashService(
            AccountsClient accountsClient,
            NotificationEventPublisher notificationEventPublisher,
            CashOperationRepository cashOperationRepository,
            JwtUsernameResolver jwtUsernameResolver,
            BusinessMetrics businessMetrics,
            @Value("${app.cash.fallback-username}") String fallbackUsername
    ) {
        this.accountsClient = accountsClient;
        this.notificationEventPublisher = notificationEventPublisher;
        this.cashOperationRepository = cashOperationRepository;
        this.jwtUsernameResolver = jwtUsernameResolver;
        this.businessMetrics = businessMetrics;
        this.fallbackUsername = fallbackUsername;
    }

    public CashOperationResponse deposit(BigDecimal amount) {
        String username = jwtUsernameResolver.resolve(fallbackUsername);
        AccountProfileView profile = accountsClient.deposit(amount);
        cashOperationRepository.save(new CashOperationEntity(username, CashOperationType.DEPOSIT, amount));
        notificationEventPublisher.send("CASH_DEPOSIT", "Deposit completed for " + username + " amount " + amount);
        return new CashOperationResponse("DEPOSIT_SUCCESS", profile.balance());
    }

    public CashOperationResponse withdraw(BigDecimal amount) {
        String username = jwtUsernameResolver.resolve(fallbackUsername);
        try {
            AccountProfileView profile = accountsClient.withdraw(amount);
            cashOperationRepository.save(new CashOperationEntity(username, CashOperationType.WITHDRAW, amount));
            notificationEventPublisher.send("CASH_WITHDRAW", "Withdraw completed for " + username + " amount " + amount);
            return new CashOperationResponse("WITHDRAW_SUCCESS", profile.balance());
        } catch (RuntimeException ex) {
            businessMetrics.recordCashWithdrawFailed(username);
            throw ex;
        }
    }
}
