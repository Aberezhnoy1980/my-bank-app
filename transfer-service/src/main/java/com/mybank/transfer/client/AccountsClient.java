package com.mybank.transfer.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybank.transfer.service.TransferOperationException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.math.BigDecimal;
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

    @CircuitBreaker(name = "accounts", fallbackMethod = "getByUsernameFallback")
    @Retry(name = "accounts")
    public AccountProfileView getByUsername(String username) {
        try {
            return restClient.get()
                    .uri("/api/accounts/{username}", username)
                    .retrieve()
                    .body(AccountProfileView.class);
        } catch (RestClientResponseException ex) {
            throw new TransferOperationException(extractErrorMessage(ex));
        }
    }

    @CircuitBreaker(name = "accounts", fallbackMethod = "withdrawFallback")
    @Retry(name = "accounts")
    public AccountProfileView withdraw(String username, BigDecimal amount) {
        return updateBalance(username, BalanceOperationType.WITHDRAW, amount);
    }

    @CircuitBreaker(name = "accounts", fallbackMethod = "depositFallback")
    @Retry(name = "accounts")
    public AccountProfileView deposit(String username, BigDecimal amount) {
        return updateBalance(username, BalanceOperationType.DEPOSIT, amount);
    }

    @SuppressWarnings("unused")
    private AccountProfileView getByUsernameFallback(String username, Throwable cause) {
        throw unavailable(cause);
    }

    @SuppressWarnings("unused")
    private AccountProfileView withdrawFallback(String username, BigDecimal amount, Throwable cause) {
        throw unavailable(cause);
    }

    @SuppressWarnings("unused")
    private AccountProfileView depositFallback(String username, BigDecimal amount, Throwable cause) {
        throw unavailable(cause);
    }

    private AccountProfileView updateBalance(String username, BalanceOperationType operationType, BigDecimal amount) {
        try {
            return restClient.put()
                    .uri("/api/accounts/{username}/balance", username)
                    .body(new UpdateBalanceRequest(operationType, amount))
                    .retrieve()
                    .body(AccountProfileView.class);
        } catch (RestClientResponseException ex) {
            throw new TransferOperationException(extractErrorMessage(ex));
        }
    }

    private TransferOperationException unavailable(Throwable cause) {
        if (cause instanceof TransferOperationException transferOperationException) {
            return transferOperationException;
        }
        return new TransferOperationException("Accounts service unavailable");
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
        return "Transfer operation failed";
    }
}
