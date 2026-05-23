package com.thex.chat.message.service;

import com.thex.chat.message.config.RabbitConfig;
import com.thex.chat.message.messaging.RoomMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageEventNotifier {

    public static final String ROOM_MESSAGE_NEW_ROUTING_KEY = "room.message.new";

    private final RabbitTemplate rabbitTemplate;

    public void notifyRoomMessage(RoomMessageEvent event) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, ROOM_MESSAGE_NEW_ROUTING_KEY, event);
        log.trace("Published {} {}", ROOM_MESSAGE_NEW_ROUTING_KEY, event);
    }
}
