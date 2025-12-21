package com.example.contract_service.publisher;

import com.example.contract_service.dto.SystemLogDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void sendLog(SystemLogDTO logDTO) {
        rabbitTemplate.convertAndSend("system.log.exchange", "system.log.routingKey", logDTO);
    }
}