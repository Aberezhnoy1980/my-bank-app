package com.mybank.accounts.client;

public record NotificationRequest(
        String eventType,
        String message
) {
}
