package com.thex.chat.chatapi.chat.room;

import com.thex.chat.chatapi.config.RabbitConfig;
import com.thex.chat.chatapi.messaging.JoinRoomRequest;
import com.thex.chat.chatapi.messaging.LeaveRoomRequest;
import com.thex.chat.chatapi.messaging.SendRoomMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomRequestNotifier {
    private final RabbitTemplate rabbitTemplate;

    public void notifyJoin(JoinRoomRequest event) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "room.join.request", event);
        log.trace("Published join event: {}", event);
    }

    public void notifyLeave(LeaveRoomRequest event) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "room.leave.request", event);
        log.trace("Published leave event: {}", event);
    }

    public void notifySendMessage(SendRoomMessageRequest event) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "room.message.send.request", event);
        log.trace("Published send event: {}", event);
    }
}
