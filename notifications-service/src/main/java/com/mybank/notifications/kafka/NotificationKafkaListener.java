package com.mybank.notifications.kafka;

import com.mybank.notification.events.NotificationEvent;
import com.mybank.notification.events.NotificationTopics;
import com.mybank.notifications.service.NotificationService;
import com.mybank.notifications.support.NotificationUsernameResolver;
import com.mybank.observability.BusinessMetrics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("@environment.getProperty('spring.kafka.bootstrap-servers', '').length() > 0")
public class NotificationKafkaListener {

    private final NotificationService notificationService;
    private final BusinessMetrics businessMetrics;

    public NotificationKafkaListener(
            NotificationService notificationService,
            BusinessMetrics businessMetrics
    ) {
        this.notificationService = notificationService;
        this.businessMetrics = businessMetrics;
    }

    @KafkaListener(
            topics = NotificationTopics.BANK_NOTIFICATIONS,
            groupId = "${spring.kafka.consumer.group-id:notifications-service}"
    )
    public void onNotification(NotificationEvent event) {
        try {
            notificationService.persist(event);
        } catch (RuntimeException ex) {
            businessMetrics.recordNotificationDeliveryFailed(NotificationUsernameResolver.resolve(event));
            throw ex;
        }
    }
}
