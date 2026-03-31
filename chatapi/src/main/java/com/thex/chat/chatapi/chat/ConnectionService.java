package com.thex.chat.chatapi.chat;

import com.thex.chat.chatapi.config.RabbitConfig;
import com.thex.chat.chatapi.messaging.SessionEvent;
import com.thex.chat.chatapi.messaging.ConnectionsUpdate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cometd.bayeux.Promise;
import org.cometd.bayeux.client.ClientSession;
import org.cometd.bayeux.server.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionService implements BayeuxServer.SessionListener {

    private final BayeuxServer bayeuxServer;
    private final RabbitTemplate rabbitTemplate;

    private ClientSession localSession;

    public static final String CHANNEL_CONNECTIONS = "/connections";

    @PostConstruct
    public void init() {
        bayeuxServer.addListener(this);
        bayeuxServer.createChannelIfAbsent(CHANNEL_CONNECTIONS);
        var local = bayeuxServer.newLocalSession("connection-updater");
        local.handshake();
        this.localSession = local;
    }

    @Override
    public void sessionAdded(ServerSession session, ServerMessage message) {
        if (session.isLocalSession()) return;

        Boolean authenticated = (Boolean) session.getAttribute(JwtHandshakePolicy.SESSION_ATTR_AUTHENTICATED);
        String username = (String) session.getAttribute(JwtHandshakePolicy.SESSION_ATTR_USERNAME);

        var event = new SessionEvent(
                session.getId(),
                "CONNECTED",
                Boolean.TRUE.equals(authenticated),
                username
        );
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "session.connected", event);
        log.info("Published session.connected for {}", username != null ? username : "guest");
    }

    @Override
    public void sessionRemoved(ServerSession session, ServerMessage message, boolean timeout) {
        if (session.isLocalSession()) return;

        var event = new SessionEvent(
                session.getId(),
                "DISCONNECTED",
                false,
                null
        );
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "session.disconnected", event);
        log.info("Published session.disconnected for session {}", session.getId());
    }

    @RabbitListener(queues = RabbitConfig.CONNECTION_UPDATES_QUEUE)
    public void onConnectionsUpdated(ConnectionsUpdate update) {
        ServerChannel channel = bayeuxServer.getChannel(CHANNEL_CONNECTIONS);
        if (channel != null) {
            Map<String, Object> data = Map.of(
                    "registered", update.registered(),
                    "guests", update.guests()
            );
            channel.publish(localSession, data, Promise.noop());
            log.info("Broadcast connection update: {} registered, {} guests",
                    update.registered().size(), update.guests().size());
        }
    }
}
