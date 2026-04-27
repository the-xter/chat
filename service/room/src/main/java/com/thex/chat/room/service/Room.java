package com.thex.chat.room.service;

import com.thex.chat.room.dto.ConnectionInfo;
import com.thex.chat.room.dto.UserInfo;
import com.thex.chat.room.dto.UserType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Room {
    private final String roomId;
    private final Map<String, ConnectionInfo> connections = new ConcurrentHashMap<>();

    public Room(String roomId) {
        this.roomId = roomId;
    }

    public String roomId() {
        return roomId;
    }

    public void addConnection(ConnectionInfo connection) {
        connections.put(connection.connectionId(), connection);
    }

    public ConnectionInfo removeConnection(String connectionId) {
        return connections.remove(connectionId);
    }

    public boolean isEmpty() {
        return connections.isEmpty();
    }

    public List<UserInfo> visibleVisitorsFor(ConnectionInfo viewer) {
        return connections.values().stream()
            .filter(subject -> isVisible(viewer, subject))
            .map(ConnectionInfo::user)
            .toList();
    }

    public List<ConnectionInfo> viewersFor(ConnectionInfo subject) {
        return connections.values().stream()
            .filter(viewer -> !viewer.connectionId().equals(subject.connectionId()))
            .filter(viewer -> isVisible(viewer, subject))
            .toList();
    }

    public List<ConnectionInfo> recipientsForLeave(ConnectionInfo leavingConnection) {
        return connections.values().stream()
            .filter(viewer -> !viewer.connectionId().equals(leavingConnection.connectionId()))
            .toList();
    }

    private boolean isVisible(ConnectionInfo viewer, ConnectionInfo subject) {
        return subject.user().type() != UserType.GUEST;
    }

    public boolean hasUser(UserInfo user) {
        return connections.values().stream()
            .anyMatch(connection -> sameUser(connection.user(), user));
    }

    private boolean sameUser(UserInfo user1, UserInfo user2) {
        return Objects.equals(user1.id(), user2.id()) && user2.type() == user1.type();
    }
}
