package com.thex.chat.room.service;

import com.thex.chat.room.config.RabbitConfig;
import com.thex.chat.room.messaging.JoinRoomEvent;
import com.thex.chat.room.messaging.LeaveRoomEvent;
import com.thex.chat.room.messaging.RoomUpdate;
import com.thex.chat.room.messaging.SessionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class RoomStateService {

    private final RabbitTemplate rabbitTemplate;

    private final Map<String, String> sessionNames = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> rooms = new ConcurrentHashMap<>();
    private final AtomicInteger guestCounter = new AtomicInteger(0);

    public RoomStateService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitConfig.SESSION_EVENTS_QUEUE)
    public void handleSessionEvent(SessionEvent event) {
        switch (event.eventType()) {
            case "CONNECTED" -> handleConnected(event);
            case "DISCONNECTED" -> handleDisconnected(event);
            default -> log.warn("Unknown event type: {}", event.eventType());
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

    void handleJoin(JoinRoomEvent event) {
        String sessionId = event.connectionInfo().connectionId();
        String name = Objects.requireNonNullElse(event.connectionInfo().user().name(), sessionId);
        sessionNames.put(sessionId, name);

        rooms.computeIfAbsent(event.roomId(), k -> ConcurrentHashMap.newKeySet())
                .add(sessionId);
        log.info("{} joined room {}", name, event.roomId());
        publishRoomUpdate(event.roomId());
    }

    void handleLeave(LeaveRoomEvent event) {
        String sessionId = event.connectionInfo().connectionId();
        String name = event.connectionInfo().user().name();
        Set<String> members = rooms.get(event.roomId());
        if (members != null) {
            members.remove(sessionId);
            log.info("{} left room {}", name, event.roomId());
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
