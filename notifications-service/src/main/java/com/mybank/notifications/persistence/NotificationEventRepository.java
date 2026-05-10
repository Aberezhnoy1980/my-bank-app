package com.mybank.notifications.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationEventRepository extends JpaRepository<NotificationEventEntity, Long> {
}
