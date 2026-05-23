package com.thex.chat.message.service;

import com.thex.chat.message.config.RabbitConfig;
import com.thex.chat.message.messaging.SendRoomMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RabbitListener(queues = RabbitConfig.ROOM_MESSAGES_QUEUE)
public class RoomMessageListener {

    private final MessageService messageService;

    @RabbitHandler
    public void handleSend(SendRoomMessageRequest request) {
        messageService.store(request);
    }

    @RabbitHandler(isDefault = true)
    public void handleDefault(Object event) {
        log.warn("Unknown room message event type: {}", event.getClass().getSimpleName());
    }
}
