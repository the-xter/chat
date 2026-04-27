package com.thex.chat.chatapi.chat;

import com.thex.chat.chatapi.dto.ConnectionInfo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.cometd.bayeux.server.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class ConnectionService implements BayeuxServer.SessionListener {

    private final BayeuxServer bayeuxServer;
    private final RestClient restClient;

    public ConnectionService(
        BayeuxServer bayeuxServer,
        @Value("${services.connection.url}") String connectionServiceUrl
    ) {
        this.bayeuxServer = bayeuxServer;
        this.restClient = RestClient.builder().baseUrl(connectionServiceUrl).build();
    }

    @PostConstruct
    public void init() {
        bayeuxServer.addListener(this);
    }

    @Override
    public void sessionAdded(ServerSession session, ServerMessage message) {
        if (session.isLocalSession()) return;

        ConnectionInfo connectionInfo = (ConnectionInfo) session.getAttribute(Consts.CONNECTION_INFO);
        if (connectionInfo == null) {
            log.warn("No ConnectionInfo for session {}", session.getId());
            return;
        }

        restClient.post()
            .uri("/connections")
            .body(connectionInfo)
            .retrieve()
            .toBodilessEntity();
        log.info("Registered connection for {}", connectionInfo.user().name());
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
}
