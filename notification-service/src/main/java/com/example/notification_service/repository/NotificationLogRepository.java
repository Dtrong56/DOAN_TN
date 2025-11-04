package com.example.notification_service.repository;

import com.example.notification_service.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, String> {
    List<NotificationLog> findByNotificationId(String notificationId);
}
