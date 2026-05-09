package com.mybank.cash.client;

public record NotificationRequest(
        String eventType,
        String message
) {
}
