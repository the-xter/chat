package com.thex.chat.auth.chat;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cometd.bayeux.Promise;
import org.cometd.bayeux.server.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class VisitorService implements BayeuxServer.SessionListener {

    private final BayeuxServer bayeuxServer;

    private final Map<String, String> registeredVisitors = new ConcurrentHashMap<>();
    private final Map<String, String> guestVisitors = new ConcurrentHashMap<>();
    private final AtomicInteger guestCounter = new AtomicInteger(0);

    public static final String CHANNEL_VISITORS = "/visitors";

    @PostConstruct
    public void init() {
        bayeuxServer.addListener(this);
        bayeuxServer.createChannelIfAbsent(CHANNEL_VISITORS);
    }

    @Override
    public void sessionAdded(ServerSession session, ServerMessage message) {
        if (session.isLocalSession()) return;

        Boolean authenticated = (Boolean) session.getAttribute(
                JwtHandshakePolicy.SESSION_ATTR_AUTHENTICATED);
        String username = (String) session.getAttribute(
                JwtHandshakePolicy.SESSION_ATTR_USERNAME);

        if (Boolean.TRUE.equals(authenticated) && username != null) {
            registeredVisitors.put(session.getId(), username);
            log.info("Registered user connected: {}", username);
        } else {
            String guestId = "Guest-" + guestCounter.incrementAndGet();
            guestVisitors.put(session.getId(), guestId);
            session.setAttribute(JwtHandshakePolicy.SESSION_ATTR_USERNAME, guestId);
            log.info("Guest connected: {}", guestId);
        }

        broadcastVisitors();
    }

    @Override
    public void sessionRemoved(ServerSession session, ServerMessage message, boolean timeout) {
        if (session.isLocalSession()) return;

        String removed = registeredVisitors.remove(session.getId());
        if (removed != null) {
            log.info("Registered user disconnected: {}", removed);
        } else {
            removed = guestVisitors.remove(session.getId());
            if (removed != null) {
                log.info("Guest disconnected: {}", removed);
            }
        }

        broadcastVisitors();
    }

    private void broadcastVisitors() {
        ServerChannel channel = bayeuxServer.getChannel(CHANNEL_VISITORS);
        if (channel != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("registered", new ArrayList<>(registeredVisitors.values()));
            data.put("guests", new ArrayList<>(guestVisitors.values()));
            channel.publish(bayeuxServer.newLocalSession("visitor-service"), data, Promise.noop());
        }
    }

    public List<String> getRegisteredVisitors() {
        return new ArrayList<>(registeredVisitors.values());
    }

    public List<String> getGuestVisitors() {
        return new ArrayList<>(guestVisitors.values());
    }
}
