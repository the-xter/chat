package com.thex.chat.connection.service;

import com.thex.chat.connection.config.RabbitConfig;
import com.thex.chat.connection.messaging.ConnectionsUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public void connect(String sessionId, boolean authenticated, String username) {
        if (authenticated && username != null) {
            registeredConnections.put(sessionId, username);
            log.info("Registered user connected: {}", username);
        } else {
            String guestId = "Guest-" + guestCounter.incrementAndGet();
            guestConnections.put(sessionId, guestId);
            log.info("Guest connected: {}", guestId);
        }
        publishUpdate("connection.connected");
    }

    public void disconnect(String sessionId) {
        String removed = registeredConnections.remove(sessionId);
        if (removed != null) {
            log.info("Registered user disconnected: {}", removed);
        } else {
            removed = guestConnections.remove(sessionId);
            if (removed != null) {
                log.info("Guest disconnected: {}", removed);
            }
        }
        publishUpdate("connection.disconnected");
    }

    private void publishUpdate(String routingKey) {
        var update = new ConnectionsUpdate(
                new ArrayList<>(registeredConnections.values()),
                new ArrayList<>(guestConnections.values())
        );
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, update);
        log.info("Published {}: {} registered, {} guests",
                routingKey, update.registered().size(), update.guests().size());
    }
}
