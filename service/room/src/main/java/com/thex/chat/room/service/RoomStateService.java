package com.thex.chat.room.service;

import com.thex.chat.room.config.RabbitConfig;
import com.thex.chat.room.dto.ConnectionInfo;
import com.thex.chat.room.dto.UserInfo;
import com.thex.chat.room.messaging.JoinRoomEvent;
import com.thex.chat.room.messaging.LeaveRoomEvent;
import com.thex.chat.room.messaging.RoomVisitors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RoomStateService {

    private final RabbitTemplate rabbitTemplate;
    private final ConnectionServiceClient connectionService;

    private final Map<String, Map<String, ConnectionInfo>> rooms = new ConcurrentHashMap<>();

    public RoomStateService(RabbitTemplate rabbitTemplate, ConnectionServiceClient connectionServiceClient) {
        this.rabbitTemplate = rabbitTemplate;
        this.connectionService = connectionServiceClient;
    }

    void handleJoin(JoinRoomEvent event) {
        log.info("join room event {}", event);

        ConnectionInfo joining = event.connectionInfo();
        String connectionId = joining.connectionId();
        if (!connectionService.isConnectionAlive(connectionId)) {
            log.info("Ignoring join for room {} because connection {} is not alive", event.roomId(), connectionId);
            return;
        }

        Map<String, ConnectionInfo> members = rooms.computeIfAbsent(
            event.roomId(), k -> new ConcurrentHashMap<>()
        );
        members.put(connectionId, joining);

        List<UserInfo> visibleVisitors = members.values().stream()
            .filter(other -> isVisible(joining, other))
            .map(ConnectionInfo::user)
            .toList();

        var message = new RoomVisitors(connectionId, event.roomId(), visibleVisitors);
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "room.visitors", message);
        log.info("Published room.visitors for {} in room {}: {} visitors",
            connectionId, event.roomId(), visibleVisitors.size());
    }

    void handleLeave(LeaveRoomEvent event) {
        log.info("leave room event {}", event);

        String connectionId = event.connectionInfo().connectionId();
        Map<String, ConnectionInfo> members = rooms.get(event.roomId());
        if (members != null) {
            members.remove(connectionId);
        }
    }

    private boolean isVisible(ConnectionInfo viewer, ConnectionInfo subject) {
        return true;
    }
}
