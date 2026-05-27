package com.mybank.notifications.service;

import com.mybank.notification.events.NotificationEvent;
import com.mybank.notifications.persistence.NotificationEventEntity;
import com.mybank.notifications.persistence.NotificationEventRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationEventRepository notificationEventRepository;

    public NotificationService(NotificationEventRepository notificationEventRepository) {
        this.notificationEventRepository = notificationEventRepository;
    }

    @Transactional
    public void persist(@Valid NotificationEvent event) {
        NotificationEventEntity entity = new NotificationEventEntity(event.eventType(), event.message());
        notificationEventRepository.save(entity);
        log.info("Notification persisted id={}, eventType={}, message={}",
                entity.getId(), event.eventType(), event.message());
    }
}
