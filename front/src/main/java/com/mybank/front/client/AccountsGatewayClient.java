package com.mybank.front.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AccountsGatewayClient {

    private final RestClient restClient;

    public AccountsGatewayClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.gateway.base-url}") String gatewayBaseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(gatewayBaseUrl).build();
    }

    public AccountProfileView getCurrentAccount() {
        return restClient.get()
                .uri("/api/accounts/me")
                .retrieve()
                .body(AccountProfileView.class);
    }

    public AccountProfileView updateCurrentAccount(UpdateAccountProfileRequest request) {
        return restClient.put()
                .uri("/api/accounts/me")
                .body(request)
                .retrieve()
                .body(AccountProfileView.class);
    }
}
