package com.example.notification_service.controller;

import com.example.notification_service.dto.NotificationRequestDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import com.example.notification_service.service.InternalNotificationService;
import lombok.RequiredArgsConstructor;
import com.example.notification_service.dto.NotificationResponseDTO;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final InternalNotificationService service;

    // Consumer cho create
    @RabbitListener(queues = "notification.queue")
    public void receiveCreate(NotificationRequestDTO dto) {
        String notificationId = service.createNotification(dto);
        System.out.println("Notification created: " + notificationId);
    }

    // Consumer cho send
    // @RabbitListener(queues = "notification.send.queue")
    // public void receiveSend(NotificationRequestDTO dto) {
    //     NotificationResponseDTO response = service.handleInternalSend(
    //         dto.getTenantId(),
    //         dto.getResidentId(),
    //         dto.getType(),
    //         dto.getMessage()
    //     );
    //     System.out.println("Notification sent: " + response);
    // }
}