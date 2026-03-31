package com.thex.chat.chatapi.chat;

import com.thex.chat.chatapi.config.RabbitConfig;
import com.thex.chat.chatapi.messaging.ConnectionsUpdate;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.cometd.bayeux.Promise;
import org.cometd.bayeux.client.ClientSession;
import org.cometd.bayeux.server.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ConnectionService implements BayeuxServer.SessionListener {

    private final BayeuxServer bayeuxServer;
    private final RestClient restClient;

    private ClientSession localSession;

    public static final String CHANNEL_CONNECTIONS = "/connections";

    public ConnectionService(BayeuxServer bayeuxServer,
                             @Value("${services.connection.url}") String connectionServiceUrl) {
        this.bayeuxServer = bayeuxServer;
        this.restClient = RestClient.builder().baseUrl(connectionServiceUrl).build();
    }

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

        var body = new HashMap<String, Object>();
        body.put("sessionId", session.getId());
        body.put("authenticated", Boolean.TRUE.equals(authenticated));
        body.put("username", username);

        restClient.post()
                .uri("/connections")
                .body(body)
                .retrieve()
                .toBodilessEntity();
        log.info("Registered connection for {}", username != null ? username : "guest");
    }

    @Override
    public void sessionRemoved(ServerSession session, ServerMessage message, boolean timeout) {
        if (session.isLocalSession()) return;

        restClient.delete()
                .uri("/connections/{sessionId}", session.getId())
                .retrieve()
                .toBodilessEntity();
        log.info("Removed connection for session {}", session.getId());
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
