package com.thex.chat.connection.service;

import com.thex.chat.connection.config.RabbitConfig;
import com.thex.chat.connection.messaging.SessionEvent;
import com.thex.chat.connection.messaging.ConnectionsUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionStateService {

    private final RabbitTemplate rabbitTemplate;

    private final Map<String, String> registeredConnections = new ConcurrentHashMap<>();
    private final Map<String, String> guestConnections = new ConcurrentHashMap<>();
    private final AtomicInteger guestCounter = new AtomicInteger(0);

    @RabbitListener(queues = RabbitConfig.SESSION_EVENTS_QUEUE)
    public void handleSessionEvent(SessionEvent event) {
        switch (event.eventType()) {
            case "CONNECTED" -> handleConnected(event);
            case "DISCONNECTED" -> handleDisconnected(event);
            default -> log.warn("Unknown event type: {}", event.eventType());
        }
        publishConnectionUpdate();
    }

    private void handleConnected(SessionEvent event) {
        if (event.authenticated() && event.username() != null) {
            registeredConnections.put(event.sessionId(), event.username());
            log.info("Registered user connected: {}", event.username());
        } else {
            String guestId = "Guest-" + guestCounter.incrementAndGet();
            guestConnections.put(event.sessionId(), guestId);
            log.info("Guest connected: {}", guestId);
        }
    }

    private void handleDisconnected(SessionEvent event) {
        String removed = registeredConnections.remove(event.sessionId());
        if (removed != null) {
            log.info("Registered user disconnected: {}", removed);
        } else {
            removed = guestConnections.remove(event.sessionId());
            if (removed != null) {
                log.info("Guest disconnected: {}", removed);
            }
        }
    }

    private void publishConnectionUpdate() {
        var update = new ConnectionsUpdate(
                new ArrayList<>(registeredConnections.values()),
                new ArrayList<>(guestConnections.values())
        );
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "connections.updated", update);
        log.info("Published connections.updated: {} registered, {} guests",
                update.registered().size(), update.guests().size());
    }
}
