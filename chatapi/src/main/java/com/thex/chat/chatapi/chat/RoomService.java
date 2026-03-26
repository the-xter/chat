package com.thex.chat.chatapi.chat;

import com.thex.chat.chatapi.config.RabbitConfig;
import com.thex.chat.chatapi.messaging.RoomEvent;
import com.thex.chat.chatapi.messaging.RoomUpdate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cometd.bayeux.Promise;
import org.cometd.bayeux.client.ClientSession;
import org.cometd.bayeux.server.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomService {

    private final BayeuxServer bayeuxServer;
    private final RabbitTemplate rabbitTemplate;

    private ClientSession localSession;

    @PostConstruct
    public void init() {
        bayeuxServer.createChannelIfAbsent("/service/room");
        ServerChannel serviceChannel = bayeuxServer.getChannel("/service/room");
        serviceChannel.addListener(new ServerChannel.MessageListener() {
            @Override
            public boolean onMessage(ServerSession from, ServerChannel channel, ServerMessage.Mutable message) {
                handleRoomRequest(from, message);
                return true;
            }
        });

        var local = bayeuxServer.newLocalSession("room-updater");
        local.handshake();
        this.localSession = local;
    }

    private void handleRoomRequest(ServerSession from, ServerMessage message) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) message.getData();
        if (data == null) return;

        String action = (String) data.get("action");
        String roomId = (String) data.get("roomId");
        if (action == null || roomId == null) return;

        String eventAction = action.toUpperCase();
        var event = new RoomEvent(from.getId(), eventAction, roomId);
        String routingKey = "JOIN".equals(eventAction) ? "room.join" : "room.leave";
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, event);
        log.info("Published {} for session {} to room {}", routingKey, from.getId(), roomId);
    }

    @RabbitListener(queues = RabbitConfig.ROOM_UPDATES_QUEUE)
    public void onRoomUpdated(RoomUpdate update) {
        String channelName = "/room/" + update.roomId();
        bayeuxServer.createChannelIfAbsent(channelName);
        ServerChannel channel = bayeuxServer.getChannel(channelName);
        if (channel != null) {
            Map<String, Object> data = Map.of(
                    "roomId", update.roomId(),
                    "members", update.members()
            );
            channel.publish(localSession, data, Promise.noop());
            log.info("Broadcast room update for {}: {} members", update.roomId(), update.members().size());
        }
    }
}
