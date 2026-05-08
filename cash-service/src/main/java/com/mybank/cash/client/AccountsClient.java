package com.mybank.cash.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybank.cash.service.CashOperationException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class AccountsClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AccountsClient(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${app.accounts.base-url}") String accountsBaseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(accountsBaseUrl).build();
        this.objectMapper = objectMapper;
    }

    public AccountProfileView deposit(BigDecimal amount) {
        return updateBalance(BalanceOperationType.DEPOSIT, amount);
    }

    public AccountProfileView withdraw(BigDecimal amount) {
        return updateBalance(BalanceOperationType.WITHDRAW, amount);
    }

    private AccountProfileView updateBalance(BalanceOperationType operationType, BigDecimal amount) {
        try {
            return restClient.put()
                    .uri("/api/accounts/me/balance")
                    .body(new UpdateBalanceRequest(operationType, amount))
                    .retrieve()
                    .body(AccountProfileView.class);
        } catch (RestClientResponseException ex) {
            throw new CashOperationException(extractErrorMessage(ex));
        }
    }

    private String extractErrorMessage(RestClientResponseException ex) {
        try {
            ApiErrorView apiError = objectMapper.readValue(ex.getResponseBodyAsByteArray(), ApiErrorView.class);
            if (apiError.errors() != null && !apiError.errors().isEmpty()) {
                return String.join("; ", apiError.errors());
            }
            if (apiError.message() != null && !apiError.message().isBlank()) {
                return apiError.message();
            }
        } catch (Exception ignored) {
            // Fallback below handles non-JSON or unexpected payload shape.
        }
        return "Cash operation failed";
    }
}
