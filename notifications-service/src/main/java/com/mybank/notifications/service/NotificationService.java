package com.mybank.notifications.service;

import com.mybank.notifications.api.NotificationRequest;
import com.mybank.notifications.api.NotificationResponse;
import com.mybank.notifications.persistence.NotificationEventEntity;
import com.mybank.notifications.persistence.NotificationEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationEventRepository notificationEventRepository;

    public NotificationService(NotificationEventRepository notificationEventRepository) {
        this.notificationEventRepository = notificationEventRepository;
    }

    @Transactional
    public NotificationResponse accept(NotificationRequest request) {
        NotificationEventEntity entity = new NotificationEventEntity(request.eventType(), request.message());
        notificationEventRepository.save(entity);
        log.info("Notification persisted id={}, eventType={}, message={}",
                entity.getId(), request.eventType(), request.message());
        return new NotificationResponse("NOTIFICATION_ACCEPTED");
    }
}
