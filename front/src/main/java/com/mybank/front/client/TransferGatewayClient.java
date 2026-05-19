package com.mybank.front.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class TransferGatewayClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TransferGatewayClient(
            @Qualifier("gatewayRestClient") RestClient restClient,
            ObjectMapper objectMapper
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public TransferResponseView transfer(TransferRequest request) {
        try {
            return restClient.post()
                    .uri("/api/transfers")
                    .body(request)
                    .retrieve()
                    .body(TransferResponseView.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 400) {
                throw new TransferValidationException(extractValidationErrors(ex));
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
        return Collections.singletonList("Transfer request failed due to validation.");
    }
}
