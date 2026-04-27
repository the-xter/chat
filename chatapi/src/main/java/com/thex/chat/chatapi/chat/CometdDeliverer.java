package com.thex.chat.chatapi.chat;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.cometd.bayeux.Promise;
import org.cometd.bayeux.client.ClientSession;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class CometdDeliverer {
    private final BayeuxServer bayeuxServer;
    private final ClientSession localSession;

    public CometdDeliverer createWith(String idHint) {
        CometdDeliverer newOne = new CometdDeliverer(bayeuxServer, idHint);
        newOne.localSession.handshake();
        return newOne;
    }

    @Autowired
    public CometdDeliverer(BayeuxServer bayeuxServer) {
        this(bayeuxServer, "cometd-server");
    }

    private CometdDeliverer(BayeuxServer bayeuxServer, String idHint) {
        this.bayeuxServer = bayeuxServer;
        this.localSession = bayeuxServer.newLocalSession(idHint);
    }

    @PostConstruct
    public void init() {
        localSession.handshake();
    }

    public void deliver(String sessionId, String channel, Object data) {
        ServerSession session = bayeuxServer.getSession(sessionId);
        if (session == null) {
            log.error("No cometd session {} to deliver in channel {}; data: {}", sessionId, channel, data);
            return;
        }
        session.deliver(localSession, channel, data, Promise.noop());
        log.info(
            "Delivered for {} in channel {}; data: {}",
            session, channel, data
        );
    }

    public void deliver(List<String> sessionIds, String channel, Object data) {
        for (var sessionId : sessionIds) {
            deliver(sessionId, channel, data);
        }
    }
}
