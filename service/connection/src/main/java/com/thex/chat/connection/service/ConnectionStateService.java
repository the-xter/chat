package com.thex.chat.connection.service;

import com.thex.chat.connection.config.RabbitConfig;
import com.thex.chat.connection.dto.ConnectionInfo;
import com.thex.chat.connection.dto.UserInfo;
import com.thex.chat.connection.dto.UserType;
import com.thex.chat.connection.messaging.ConnectionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionStateService {

    private final RabbitTemplate rabbitTemplate;

    private final Map<String, ConnectionInfo> connections = new ConcurrentHashMap<>();
    private final AtomicInteger guestCounter = new AtomicInteger(0);

    public void connect(ConnectionInfo connectionInfo) {
        UserInfo user = connectionInfo.user();
        if (user.type() == UserType.GUEST) {
            String guestName = "Guest-" + guestCounter.incrementAndGet();
            user = new UserInfo(connectionInfo.connectionId(), guestName, UserType.GUEST);
            connectionInfo = new ConnectionInfo(connectionInfo.connectionId(), user);
        }
        connections.put(connectionInfo.connectionId(), connectionInfo);
        log.info("{} connected: {}", user.type(), user.name());
        publishEvent("connection.connected", connectionInfo);
    }

    public void disconnect(String connectionId) {
        ConnectionInfo removed = connections.remove(connectionId);
        if (removed != null) {
            log.info("{} disconnected: {}", removed.user().type(), removed.user().name());
            publishEvent("connection.disconnected", removed);
        }
    }

    private void publishEvent(String routingKey, ConnectionInfo connectionInfo) {
        var event = new ConnectionEvent(routingKey.substring("connection.".length()).toUpperCase(), connectionInfo);
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, event);
        log.info("Published {}: {}", routingKey, connectionInfo.user().name());
    }
}
