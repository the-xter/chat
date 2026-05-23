package com.thex.chat.room.service;

import com.thex.chat.room.config.RabbitConfig;
import com.thex.chat.room.messaging.ConnectionEvent;
import com.thex.chat.room.messaging.JoinRoomRequest;
import com.thex.chat.room.messaging.LeaveRoomRequest;
import com.thex.chat.room.messaging.RoomMessageEvent;
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

    private final RoomService roomService;

    @RabbitHandler
    public void handleJoin(JoinRoomRequest event) {
        roomService.handleJoin(event);
    }

    @RabbitHandler
    public void handleLeave(LeaveRoomRequest event) {
        roomService.handleLeave(event);
    }

    @RabbitHandler
    public void handleRoomMessage(RoomMessageEvent event) {
        roomService.handleRoomMessage(event);
    }

    @RabbitHandler
    public void handleConnectionEvent(ConnectionEvent event) {
        switch (event.eventType()) {
            case "CONNECTED" -> roomService.handleConnect(event.connection());
            case "DISCONNECTED" -> roomService.handleDisconnect(event.connection());
            default -> log.warn("Unknown connection event type: {}", event.eventType());
        }
    }

    @RabbitHandler(isDefault = true)
    public void handleDefault(Object event) {
        log.warn("Unknown room event type: {}", event.getClass().getSimpleName());
    }
}
