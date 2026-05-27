package com.mybank.notifications.kafka;

import com.mybank.notification.events.NotificationEvent;
import com.mybank.notification.events.NotificationTopics;
import com.mybank.notifications.persistence.NotificationEventRepository;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics = {NotificationTopics.BANK_NOTIFICATIONS},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class NotificationKafkaListenerIntegrationTest {

    @Autowired
    private NotificationEventRepository notificationEventRepository;

    @Autowired
    private org.springframework.kafka.test.EmbeddedKafkaBroker embeddedKafkaBroker;

    private KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @BeforeEach
    void setUp() {
        notificationEventRepository.deleteAll();
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps));
    }

    @Test
    void shouldPersistNotificationEventFromKafka() throws InterruptedException {
        kafkaTemplate.send(
                NotificationTopics.BANK_NOTIFICATIONS,
                new NotificationEvent("CASH_DEPOSIT", "Deposit completed")
        );

        for (int attempt = 0; attempt < 50 && notificationEventRepository.count() == 0; attempt++) {
            Thread.sleep(200);
        }

        assertThat(notificationEventRepository.findAll())
                .singleElement()
                .satisfies(entity -> {
                    assertThat(entity.getEventType()).isEqualTo("CASH_DEPOSIT");
                    assertThat(entity.getMessage()).isEqualTo("Deposit completed");
                });
    }
}
