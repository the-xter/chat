package com.thex.chat.room.service;

import com.thex.chat.room.dto.ConnectionInfo;
import com.thex.chat.room.dto.UserInfo;
import com.thex.chat.room.dto.UserType;
import com.thex.chat.room.messaging.JoinRoomRequest;
import com.thex.chat.room.messaging.LeaveRoomRequest;
import com.thex.chat.room.messaging.RoomNotifier;
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

    void handleJoin(JoinRoomRequest event) {
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

        sendRoomVisitors(joiningConnection, event.roomId(), members);
        notifyVisitorJoined(members, event.roomId(), joiningConnection);
    }

    private void sendRoomVisitors(ConnectionInfo connection, String roomId, Map<String, ConnectionInfo> members) {
        List<UserInfo> visibleVisitors = members.values().stream()
            .filter(other -> isVisible(connection, other))
            .map(ConnectionInfo::user)
            .toList();
        roomNotifier.sendRoomVisitors(connection.connectionId(), roomId, visibleVisitors);
    }

    private void notifyVisitorJoined(
        Map<String, ConnectionInfo> members,
        String roomId,
        ConnectionInfo joiningConnection
    ) {
        List<ConnectionInfo> viewers = filterConnectionsWhoCanSee(joiningConnection, members);
        if (!viewers.isEmpty()) {
            roomNotifier.notifyVisitorJoined(
                viewers,
                roomId,
                joiningConnection.user()
            );
        }
    }

    private List<ConnectionInfo> filterConnectionsWhoCanSee(
        ConnectionInfo connection,
        Map<String, ConnectionInfo> members
    ) {
        return members.values().stream()
            .filter(viewer -> !viewer.connectionId().equals(connection.connectionId()))
            .filter(viewer -> isVisible(viewer, connection))
            .toList();
    }

    void handleLeave(LeaveRoomRequest event) {
        log.info("leave room event {}", event);

        ConnectionInfo leavingConnection = event.connectionInfo();
        String connectionId = leavingConnection.connectionId();
        Map<String, ConnectionInfo> members = rooms.get(event.roomId());
        if (members == null) {
            return;
        }

        ConnectionInfo removed = members.remove(connectionId);
        if (removed == null) {
            return;
        }

        notifyVisitorLeft(members, event.roomId(), leavingConnection);
    }

    private void notifyVisitorLeft(
        Map<String, ConnectionInfo> members,
        String roomId,
        ConnectionInfo leavingConnection
    ) {
        if (!members.isEmpty()) {
            roomNotifier.notifyVisitorLeft(
                members.values(),
                roomId,
                leavingConnection.user()
            );
        }
    }

    private boolean isVisible(ConnectionInfo viewer, ConnectionInfo subject) {
        return subject.user().type() != UserType.GUEST;
    }
}
