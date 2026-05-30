package com.mybank.observability;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Custom business counters required by sprint 12 (grouped by login / participants).
 */
public class BusinessMetrics {

    public static final String CASH_WITHDRAW_FAILED = "mybank.cash.withdraw.failed";
    public static final String TRANSFER_FAILED = "mybank.transfer.failed";
    public static final String NOTIFICATION_DELIVERY_FAILED = "mybank.notification.delivery.failed";

    private final MeterRegistry registry;

    public BusinessMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordCashWithdrawFailed(String username) {
        registry.counter(CASH_WITHDRAW_FAILED, "username", tag(username)).increment();
    }

    public void recordTransferFailed(String sender, String recipient) {
        registry.counter(
                TRANSFER_FAILED,
                "sender", tag(sender),
                "recipient", tag(recipient)
        ).increment();
    }

    public void recordNotificationDeliveryFailed(String username) {
        registry.counter(NOTIFICATION_DELIVERY_FAILED, "username", tag(username)).increment();
    }

    private static String tag(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value;
    }
}
