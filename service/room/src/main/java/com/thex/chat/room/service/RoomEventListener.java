package com.thex.chat.room.service;

import com.thex.chat.room.config.RabbitConfig;
import com.thex.chat.room.messaging.ConnectionEvent;
import com.thex.chat.room.messaging.JoinRoomRequest;
import com.thex.chat.room.messaging.LeaveRoomRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RabbitListener(queues = RabbitConfig.ROOM_EVENTS_QUEUE)
public class RoomEventListener {

    private final RoomStateService roomStateService;

    @RabbitHandler
    public void handleJoin(JoinRoomRequest event) {
        roomStateService.handleJoin(event);
    }

    @RabbitHandler
    public void handleLeave(LeaveRoomRequest event) {
        roomStateService.handleLeave(event);
    }

    @RabbitHandler
    public void handleConnectionEvent(ConnectionEvent event) {
        if ("DISCONNECTED".equals(event.eventType())) {
            roomStateService.handleDisconnect(event.connection().connectionId());
        }
    }

    @RabbitHandler(isDefault = true)
    public void handleDefault(Object event) {
        log.warn("Unknown room event type: {}", event.getClass().getSimpleName());
    }
}
