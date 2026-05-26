package com.mybank.notification.events;

import jakarta.validation.constraints.NotBlank;

/**
 * JSON payload for topic {@link NotificationTopics#BANK_NOTIFICATIONS}.
 */
public record NotificationEvent(
        @NotBlank String eventType,
        @NotBlank String message
) {
}
