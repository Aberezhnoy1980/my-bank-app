package com.mybank.cash.service;

import com.mybank.cash.api.CashOperationResponse;
import com.mybank.cash.client.AccountProfileView;
import com.mybank.cash.client.AccountsClient;
import com.mybank.cash.client.NotificationsClient;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class CashService {

    private final AccountsClient accountsClient;
    private final NotificationsClient notificationsClient;

    public CashService(
            AccountsClient accountsClient,
            NotificationsClient notificationsClient
    ) {
        this.accountsClient = accountsClient;
        this.notificationsClient = notificationsClient;
    }

    public CashOperationResponse deposit(BigDecimal amount) {
        AccountProfileView profile = accountsClient.deposit(amount);
        notificationsClient.send("CASH_DEPOSIT", "Deposit completed for demo.user amount " + amount);
        return new CashOperationResponse("DEPOSIT_SUCCESS", profile.balance());
    }

    public CashOperationResponse withdraw(BigDecimal amount) {
        AccountProfileView profile = accountsClient.withdraw(amount);
        notificationsClient.send("CASH_WITHDRAW", "Withdraw completed for demo.user amount " + amount);
        return new CashOperationResponse("WITHDRAW_SUCCESS", profile.balance());
    }
}
