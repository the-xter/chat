package com.thex.chat.room.service;

import com.thex.chat.room.config.RabbitConfig;
import com.thex.chat.room.messaging.JoinRoomEvent;
import com.thex.chat.room.messaging.LeaveRoomEvent;
import com.thex.chat.room.messaging.RoomUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RoomStateService {

    private final RabbitTemplate rabbitTemplate;

    private final Map<String, String> sessionNames = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> rooms = new ConcurrentHashMap<>();

    public RoomStateService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    void handleJoin(JoinRoomEvent event) {
        log.info("join room event {}", event);

        String sessionId = event.connectionInfo().connectionId();
        String name = Objects.requireNonNullElse(event.connectionInfo().user().name(), sessionId);
        sessionNames.put(sessionId, name);

        rooms.computeIfAbsent(event.roomId(), k -> ConcurrentHashMap.newKeySet())
                .add(sessionId);
        publishRoomUpdate(event.roomId());
    }

    void handleLeave(LeaveRoomEvent event) {
        log.info("leave room event {}", event);

        String sessionId = event.connectionInfo().connectionId();
        Set<String> members = rooms.get(event.roomId());
        if (members != null) {
            members.remove(sessionId);
            publishRoomUpdate(event.roomId());
        }
    }

    private void publishRoomUpdate(String roomId) {
        Set<String> sessionIds = rooms.getOrDefault(roomId, Set.of());
        List<String> memberNames = sessionIds.stream()
                .map(sessionNames::get)
                .filter(Objects::nonNull)
                .sorted()
                .toList();
        var update = new RoomUpdate(roomId, memberNames);
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "room.updated", update);
        log.info("Published room.updated for {}: {} members", roomId, memberNames.size());
    }
}
