package com.mybank.cash.service;

import com.mybank.cash.api.CashOperationResponse;
import com.mybank.cash.client.AccountProfileView;
import com.mybank.cash.client.AccountsClient;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class CashService {

    private final AccountsClient accountsClient;

    public CashService(AccountsClient accountsClient) {
        this.accountsClient = accountsClient;
    }

    public CashOperationResponse deposit(BigDecimal amount) {
        AccountProfileView profile = accountsClient.deposit(amount);
        return new CashOperationResponse("DEPOSIT_SUCCESS", profile.balance());
    }

    public CashOperationResponse withdraw(BigDecimal amount) {
        AccountProfileView profile = accountsClient.withdraw(amount);
        return new CashOperationResponse("WITHDRAW_SUCCESS", profile.balance());
    }
}
