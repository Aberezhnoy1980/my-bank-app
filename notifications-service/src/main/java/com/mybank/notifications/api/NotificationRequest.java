package com.mybank.notifications.api;

import jakarta.validation.constraints.NotBlank;

public record NotificationRequest(
        @NotBlank(message = "eventType must not be blank")
        String eventType,
        @NotBlank(message = "message must not be blank")
        String message
) {
}
