package com.thex.chat.chatapi.chat.room;

import com.thex.chat.chatapi.config.RabbitConfig;
import com.thex.chat.chatapi.messaging.RoomVisitorUpdate;
import com.thex.chat.chatapi.messaging.RoomVisitors;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.cometd.bayeux.Promise;
import org.cometd.bayeux.client.ClientSession;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerSession;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
@RabbitListener(queues = RabbitConfig.ROOM_VISITORS_QUEUE)
public class RoomVisitorsListener {

    private final BayeuxServer bayeuxServer;
    private ClientSession localSession;

    public RoomVisitorsListener(BayeuxServer bayeuxServer) {
        this.bayeuxServer = bayeuxServer;
    }

    @PostConstruct
    public void init() {
        var local = bayeuxServer.newLocalSession("room-visitors");
        local.handshake();
        this.localSession = local;
    }

    @RabbitHandler
    public void onRoomVisitors(RoomVisitors message) {
        ServerSession session = resolveSession(message.targetConnectionId(), message.roomId(), "visitors");
        if (session == null) {
            return;
        }

        Map<String, Object> data = Map.of(
            "roomId", message.roomId(),
            "visitors", message.visitors()
        );
        session.deliver(localSession, "/room/" + message.roomId(), data, Promise.noop());
        log.info("Delivered {} visitors to {} for room {}",
            message.visitors().size(), message.targetConnectionId(), message.roomId());
    }

    @RabbitHandler
    public void onRoomVisitorUpdate(RoomVisitorUpdate update) {
        for (var target : update.recipients()) {
            ServerSession session = resolveSession(target, update.roomId(), "visitor update");
            if (session == null) {
                return;
            }

            Map<String, Object> data = Map.of(
                "roomId", update.roomId(),
                "user", update.user(),
                "action", update.action().name().toLowerCase(Locale.ROOT)
            );
            session.deliver(localSession, "/room/" + update.roomId() + "/visitor", data, Promise.noop());
            log.info("Delivered visitor {} to {} for room {}: {}",
                update.action(), target, update.roomId(), update.user());
        }
    }

    @RabbitHandler(isDefault = true)
    public void onUnknown(Object message) {
        log.warn("Unknown room visitors message type: {}", message.getClass().getSimpleName());
    }

    private ServerSession resolveSession(String connectionId, String roomId, String kind) {
        ServerSession session = bayeuxServer.getSession(connectionId);
        if (session == null) {
            log.info("No cometd session {} to deliver {} for room {}", connectionId, kind, roomId);
        }
        return session;
    }
}
