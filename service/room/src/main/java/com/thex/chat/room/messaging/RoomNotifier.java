package com.thex.chat.room.messaging;

import com.thex.chat.room.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomNotifier {
    private final RabbitTemplate rabbitTemplate;

    public void sendRoomVisitors(RoomVisitors roomVisitors) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "room.visitors", roomVisitors);
        log.info("Published room.visitors {}", roomVisitors);
    }
}
