package com.example.service_catalog_service.publisher;

import com.example.service_catalog_service.dto.NotificationRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishNotification(NotificationRequestDTO dto) {
        rabbitTemplate.convertAndSend(
            "notification.exchange",
            "notification.routingKey",
            dto
        );
    }
}
