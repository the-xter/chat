package com.thex.chat.visitor.service;

import com.thex.chat.visitor.config.RabbitConfig;
import com.thex.chat.visitor.messaging.SessionEvent;
import com.thex.chat.visitor.messaging.VisitorsUpdate;
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
public class VisitorStateService {

    private final RabbitTemplate rabbitTemplate;

    private final Map<String, String> registeredVisitors = new ConcurrentHashMap<>();
    private final Map<String, String> guestVisitors = new ConcurrentHashMap<>();
    private final AtomicInteger guestCounter = new AtomicInteger(0);

    @RabbitListener(queues = RabbitConfig.SESSION_EVENTS_QUEUE)
    public void handleSessionEvent(SessionEvent event) {
        switch (event.eventType()) {
            case "CONNECTED" -> handleConnected(event);
            case "DISCONNECTED" -> handleDisconnected(event);
            default -> log.warn("Unknown event type: {}", event.eventType());
        }
        publishVisitorUpdate();
    }

    private void handleConnected(SessionEvent event) {
        if (event.authenticated() && event.username() != null) {
            registeredVisitors.put(event.sessionId(), event.username());
            log.info("Registered user connected: {}", event.username());
        } else {
            String guestId = "Guest-" + guestCounter.incrementAndGet();
            guestVisitors.put(event.sessionId(), guestId);
            log.info("Guest connected: {}", guestId);
        }
    }

    private void handleDisconnected(SessionEvent event) {
        String removed = registeredVisitors.remove(event.sessionId());
        if (removed != null) {
            log.info("Registered user disconnected: {}", removed);
        } else {
            removed = guestVisitors.remove(event.sessionId());
            if (removed != null) {
                log.info("Guest disconnected: {}", removed);
            }
        }
    }

    private void publishVisitorUpdate() {
        var update = new VisitorsUpdate(
                new ArrayList<>(registeredVisitors.values()),
                new ArrayList<>(guestVisitors.values())
        );
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "visitors.updated", update);
        log.info("Published visitors.updated: {} registered, {} guests",
                update.registered().size(), update.guests().size());
    }
}
