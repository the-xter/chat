package com.thex.chat.chatapi.chat.room;

import com.thex.chat.chatapi.chat.CometdDeliverer;
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

    private final CometdDeliverer deliverer;

    public RoomVisitorsListener(CometdDeliverer deliverer) {
        this.deliverer = deliverer.createWith("room-visitors");
    }

    @RabbitHandler
    public void onRoomVisitors(RoomVisitors message) {
        deliverer.deliver(
            message.targetConnectionId(),
            "/room/" + message.roomId(),
            toTransport(message)
        );
    }

    private Object toTransport(RoomVisitors message) {
        return Map.of(
            "roomId", message.roomId(),
            "visitors", message.visitors()
        );
    }

    @RabbitHandler
    public void onRoomVisitorUpdate(RoomVisitorUpdate update) {
        deliverer.deliver(
            update.recipients(),
            "/room/" + update.roomId() + "/visitor",
            toTransport(update)
        );
    }

    private Object toTransport(RoomVisitorUpdate update) {
        return Map.of(
            "roomId", update.roomId(),
            "user", update.user(),
            "action", update.action().name().toLowerCase(Locale.ROOT)
        );
    }

    @RabbitHandler(isDefault = true)
    public void onUnknown(Object message) {
        log.warn("Unknown room visitors message type: {}", message.getClass().getSimpleName());
    }
}
