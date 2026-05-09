package com.mybank.notifications.api;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationsController {

    private static final Logger log = LoggerFactory.getLogger(NotificationsController.class);

    @PostMapping
    public NotificationResponse notify(@Valid @RequestBody NotificationRequest request) {
        log.info("Notification eventType={}, message={}", request.eventType(), request.message());
        return new NotificationResponse("NOTIFICATION_ACCEPTED");
    }
}
