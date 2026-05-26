package com.mybank.cash.kafka;

import com.mybank.notification.events.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class NotificationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventPublisher.class);

    private final Optional<KafkaTemplate<String, NotificationEvent>> kafkaTemplate;
    private final String topic;

    public NotificationEventPublisher(
            @Autowired(required = false) KafkaTemplate<String, NotificationEvent> kafkaTemplate,
            @Value("${app.kafka.topic:bank.notifications}") String topic
    ) {
        this.kafkaTemplate = Optional.ofNullable(kafkaTemplate);
        this.topic = topic;
    }

    public void send(String eventType, String message) {
        kafkaTemplate.ifPresentOrElse(
                template -> {
                    try {
                        template.send(topic, new NotificationEvent(eventType, message));
                    } catch (Exception ex) {
                        log.warn("Failed to publish notification event, eventType={}", eventType, ex);
                    }
                },
                () -> log.debug("Kafka not configured, skip notification eventType={}", eventType)
        );
    }
}
