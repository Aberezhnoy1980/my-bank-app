package com.mybank.transfer.client;

public record NotificationRequest(
        String eventType,
        String message
) {
}
