package com.mybank.front.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class AccountsGatewayClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AccountsGatewayClient(
            @Qualifier("gatewayRestClient") RestClient restClient,
            ObjectMapper objectMapper
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public AccountProfileView getCurrentAccount() {
        return restClient.get()
                .uri("/api/accounts/me")
                .retrieve()
                .body(AccountProfileView.class);
    }

    public AccountProfileView updateCurrentAccount(UpdateAccountProfileRequest request) {
        try {
            return restClient.put()
                    .uri("/api/accounts/me")
                    .body(request)
                    .retrieve()
                    .body(AccountProfileView.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 400) {
                throw new AccountUpdateValidationException(extractValidationErrors(ex));
            }
            throw ex;
        }
    }

    private List<String> extractValidationErrors(RestClientResponseException ex) {
        try {
            ApiErrorView apiError = objectMapper.readValue(ex.getResponseBodyAsByteArray(), ApiErrorView.class);
            if (apiError.errors() != null && !apiError.errors().isEmpty()) {
                return apiError.errors();
            }
            if (apiError.message() != null && !apiError.message().isBlank()) {
                return List.of(apiError.message());
            }
        } catch (Exception ignored) {
            // Fallback below handles non-JSON or unexpected payload shape.
        }
        return Collections.singletonList("Profile update failed due to validation.");
    }
}
