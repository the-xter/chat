package com.thex.chat.chatapi.chat;

import com.thex.chat.chatapi.config.RabbitConfig;
import com.thex.chat.chatapi.dto.ConnectionInfo;
import com.thex.chat.chatapi.dto.UserInfo;
import com.thex.chat.chatapi.dto.UserType;
import com.thex.chat.chatapi.messaging.ConnectionEvent;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.cometd.bayeux.Promise;
import org.cometd.bayeux.client.ClientSession;
import org.cometd.bayeux.server.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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

        UserType userType = Boolean.TRUE.equals(authenticated) ? UserType.REGISTERED : UserType.GUEST;
        var user = new UserInfo(username, username, userType);
        var connectionInfo = new ConnectionInfo(session.getId(), user);

        session.setAttribute(Consts.CONNECTION_INFO, connectionInfo);

        restClient.post()
            .uri("/connections")
            .body(connectionInfo)
            .retrieve()
            .toBodilessEntity();
        log.info("Registered connection for {}", username != null ? username : "guest");
    }

    @Override
    public void sessionRemoved(ServerSession session, ServerMessage message, boolean timeout) {
        if (session.isLocalSession()) return;

        restClient.delete()
            .uri("/connections/{connectionId}", session.getId())
            .retrieve()
            .toBodilessEntity();
        log.info("Removed connection for session {}", session.getId());
    }

    @RabbitListener(queues = RabbitConfig.CONNECTION_UPDATES_QUEUE)
    public void onConnectionEvent(ConnectionEvent event) {
        ServerChannel channel = bayeuxServer.getChannel(CHANNEL_CONNECTIONS);
        if (channel != null) {
            Map<String, Object> data = Map.of(
                "eventType", event.eventType(),
                "connection", Map.of(
                    "connectionId", event.connection().connectionId(),
                    "user", Map.of(
                        "id", event.connection().user().id(),
                        "name", event.connection().user().name(),
                        "type", event.connection().user().type().name()
                    )
                )
            );
            channel.publish(localSession, data, Promise.noop());
            log.info("Broadcast connection {}: {}", event.eventType(), event.connection().user().name());
        }
    }
}
