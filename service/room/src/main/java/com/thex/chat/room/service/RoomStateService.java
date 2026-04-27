package com.thex.chat.room.service;

import com.thex.chat.room.dto.ConnectionInfo;
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

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    void handleJoin(JoinRoomRequest event) {
        log.info("join room event {}", event);

        ConnectionInfo joiningConnection = event.connectionInfo();
        String connectionId = joiningConnection.connectionId();
        if (!connectionService.isConnectionAlive(connectionId)) {
            log.info("Ignoring join for room {} because connection {} is not alive", event.roomId(), connectionId);
            return;
        }

        Room room = rooms.computeIfAbsent(event.roomId(), Room::new);
        room.addConnection(joiningConnection);

        sendRoomVisitors(room, joiningConnection);
        notifyVisitorJoined(room, joiningConnection);
    }

    private void sendRoomVisitors(Room room, ConnectionInfo connection) {
        roomNotifier.sendRoomVisitors(
            connection.connectionId(),
            room.roomId(),
            room.visibleVisitorsFor(connection)
        );
    }

    private void notifyVisitorJoined(Room room, ConnectionInfo joiningConnection) {
        List<ConnectionInfo> viewers = room.viewersFor(joiningConnection);
        if (!viewers.isEmpty()) {
            roomNotifier.notifyVisitorJoined(
                viewers,
                room.roomId(),
                joiningConnection.user()
            );
        }
    }

    void handleLeave(LeaveRoomRequest event) {
        log.info("leave room event {}", event);

        ConnectionInfo leavingConnection = event.connectionInfo();
        Room room = rooms.get(event.roomId());
        if (room == null) {
            return;
        }
        removeFromRoom(room, leavingConnection);
    }

    void handleDisconnect(ConnectionInfo connection) {
        log.info("disconnect event for connection {}", connection);
        rooms.values().forEach(room -> removeFromRoom(room, connection));
    }

    private void removeFromRoom(Room room, ConnectionInfo connection) {
        ConnectionInfo removed = room.removeConnection(connection.connectionId());
        if (removed == null) {
            return;
        }
        if (room.hasUser(connection.user())) {  //a single user can have multiple connections in a room
            return;
        }
        notifyVisitorLeft(room, removed);
        if (room.isEmpty()) {
            rooms.remove(room.roomId(), room);
        }
    }

    private void notifyVisitorLeft(Room room, ConnectionInfo leavingConnection) {
        List<ConnectionInfo> recipients = room.recipientsForLeave(leavingConnection);
        if (!recipients.isEmpty()) {
            roomNotifier.notifyVisitorLeft(
                recipients,
                room.roomId(),
                leavingConnection.user()
            );
        }
    }
}
