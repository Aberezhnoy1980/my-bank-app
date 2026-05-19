package com.mybank.cash.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybank.cash.service.CashOperationException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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

    @CircuitBreaker(name = "accounts", fallbackMethod = "depositFallback")
    @Retry(name = "accounts")
    public AccountProfileView deposit(BigDecimal amount) {
        return updateBalance(BalanceOperationType.DEPOSIT, amount);
    }

    @CircuitBreaker(name = "accounts", fallbackMethod = "withdrawFallback")
    @Retry(name = "accounts")
    public AccountProfileView withdraw(BigDecimal amount) {
        return updateBalance(BalanceOperationType.WITHDRAW, amount);
    }

    @SuppressWarnings("unused")
    private AccountProfileView depositFallback(BigDecimal amount, Throwable cause) {
        throw unavailable(cause);
    }

    @SuppressWarnings("unused")
    private AccountProfileView withdrawFallback(BigDecimal amount, Throwable cause) {
        throw unavailable(cause);
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

    private CashOperationException unavailable(Throwable cause) {
        if (cause instanceof CashOperationException cashOperationException) {
            return cashOperationException;
        }
        return new CashOperationException("Accounts service unavailable");
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
