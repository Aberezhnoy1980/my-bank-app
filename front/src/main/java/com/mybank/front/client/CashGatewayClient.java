package com.mybank.front.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class CashGatewayClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public CashGatewayClient(
            @Qualifier("gatewayRestClient") RestClient restClient,
            ObjectMapper objectMapper
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public CashOperationResponseView deposit(BigDecimal amount) {
        return call("/api/cash/deposit", amount);
    }

    public CashOperationResponseView withdraw(BigDecimal amount) {
        return call("/api/cash/withdraw", amount);
    }

    private CashOperationResponseView call(String uri, BigDecimal amount) {
        try {
            return restClient.post()
                    .uri(uri)
                    .body(new CashOperationRequest(amount))
                    .retrieve()
                    .body(CashOperationResponseView.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 400) {
                throw new CashValidationException(extractValidationErrors(ex));
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
        return Collections.singletonList("Cash operation failed due to validation.");
    }
}
