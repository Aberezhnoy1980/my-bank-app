package com.mybank.accounts.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NotificationsClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationsClient.class);
    private final RestClient restClient;

    public NotificationsClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.notifications.base-url}") String notificationsBaseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(notificationsBaseUrl).build();
    }

    public void send(String eventType, String message) {
        try {
            restClient.post()
                    .uri("/api/notifications")
                    .body(new NotificationRequest(eventType, message))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.warn("Notifications service unavailable, eventType={}", eventType);
        }
    }
}
