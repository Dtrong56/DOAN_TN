package com.example.notification_service.repository;

import com.example.notification_service.entity.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, String> {
    List<NotificationChannel> findByTenantIdAndActiveTrue(String tenantId);
}
