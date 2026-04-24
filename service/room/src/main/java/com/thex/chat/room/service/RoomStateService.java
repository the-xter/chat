package com.thex.chat.room.service;

import com.thex.chat.room.dto.ConnectionInfo;
import com.thex.chat.room.dto.UserInfo;
import com.thex.chat.room.messaging.JoinRoomEvent;
import com.thex.chat.room.messaging.LeaveRoomEvent;
import com.thex.chat.room.messaging.RoomNotifier;
import com.thex.chat.room.messaging.RoomVisitors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomStateService {

    private final RoomNotifier roomNotifier;
    private final ConnectionServiceClient connectionService;

    private final Map<String, Map<String, ConnectionInfo>> rooms = new ConcurrentHashMap<>();

    void handleJoin(JoinRoomEvent event) {
        log.info("join room event {}", event);

        ConnectionInfo joiningConnection = event.connectionInfo();
        String connectionId = joiningConnection.connectionId();
        if (!connectionService.isConnectionAlive(connectionId)) {
            log.info("Ignoring join for room {} because connection {} is not alive", event.roomId(), connectionId);
            return;
        }

        Map<String, ConnectionInfo> members = rooms.computeIfAbsent(
            event.roomId(), k -> new ConcurrentHashMap<>()
        );
        members.put(connectionId, joiningConnection);

        List<UserInfo> visibleVisitors = members.values().stream()
            .filter(other -> isVisible(joiningConnection, other))
            .map(ConnectionInfo::user)
            .toList();

        roomNotifier.sendRoomVisitors(new RoomVisitors(connectionId, event.roomId(), visibleVisitors));
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
