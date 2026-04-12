package com.thex.chat.chatapi.chat.room;

import com.thex.chat.chatapi.config.RabbitConfig;
import com.thex.chat.chatapi.messaging.JoinRoomEvent;
import com.thex.chat.chatapi.messaging.LeaveRoomEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomNotifier {
    private final RabbitTemplate rabbitTemplate;

    public void notifyJoin(JoinRoomEvent event) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "room.join", event);
        log.info("Published join event: {}", event);
    }

    public void notifyLeave(LeaveRoomEvent event) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "room.leave", event);
        log.info("Published leave event: {}", event);
    }
}
