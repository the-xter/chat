package com.thex.chat.room.service;

import com.thex.chat.room.config.RabbitConfig;
import com.thex.chat.room.messaging.RoomEvent;
import com.thex.chat.room.messaging.RoomUpdate;
import com.thex.chat.room.messaging.SessionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomStateService {

    private final RabbitTemplate rabbitTemplate;

    private final Map<String, String> sessionNames = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> rooms = new ConcurrentHashMap<>();
    private final AtomicInteger guestCounter = new AtomicInteger(0);

    @RabbitListener(queues = RabbitConfig.SESSION_EVENTS_QUEUE)
    public void handleSessionEvent(SessionEvent event) {
        switch (event.eventType()) {
            case "CONNECTED" -> handleConnected(event);
            case "DISCONNECTED" -> handleDisconnected(event);
            default -> log.warn("Unknown event type: {}", event.eventType());
        }
    }

    @RabbitListener(queues = RabbitConfig.ROOM_EVENTS_QUEUE)
    public void handleRoomEvent(RoomEvent event) {
        switch (event.action()) {
            case "JOIN" -> handleJoin(event);
            case "LEAVE" -> handleLeave(event);
            default -> log.warn("Unknown room action: {}", event.action());
        }
    }

    private void handleConnected(SessionEvent event) {
        if (event.authenticated() && event.username() != null) {
            sessionNames.put(event.sessionId(), event.username());
            log.info("Tracked session for registered user: {}", event.username());
        } else {
            String guestId = "Guest-" + guestCounter.incrementAndGet();
            sessionNames.put(event.sessionId(), guestId);
            log.info("Tracked session for guest: {}", guestId);
        }
    }

    private void handleDisconnected(SessionEvent event) {
        String name = sessionNames.remove(event.sessionId());
        if (name != null) {
            log.info("Session removed: {}", name);
            for (var entry : rooms.entrySet()) {
                if (entry.getValue().remove(event.sessionId())) {
                    log.info("Removed {} from room {}", name, entry.getKey());
                    publishRoomUpdate(entry.getKey());
                }
            }
        }
    }

    private void handleJoin(RoomEvent event) {
        String name = sessionNames.get(event.sessionId());
        if (name == null) {
            log.warn("Join from unknown session: {}", event.sessionId());
            return;
        }
        rooms.computeIfAbsent(event.roomId(), k -> ConcurrentHashMap.newKeySet())
                .add(event.sessionId());
        log.info("{} joined room {}", name, event.roomId());
        publishRoomUpdate(event.roomId());
    }

    private void handleLeave(RoomEvent event) {
        String name = sessionNames.get(event.sessionId());
        Set<String> members = rooms.get(event.roomId());
        if (members != null) {
            members.remove(event.sessionId());
            log.info("{} left room {}", name != null ? name : event.sessionId(), event.roomId());
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
