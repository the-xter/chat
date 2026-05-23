package com.thex.chat.room.service;

import com.thex.chat.room.dto.ConnectionInfo;
import com.thex.chat.room.dto.UserInfo;
import com.thex.chat.room.dto.UserType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    public Collection<UserInfo> visibleVisitorsFor(ConnectionInfo viewer) {
        return connections.values().stream()
            .filter(subject -> isVisible(viewer, subject))
            .map(ConnectionInfo::user)
            .distinct()  //the equals method is overridden for records
            .toList();
    }

    public List<ConnectionInfo> viewersFor(ConnectionInfo subject) {
        return connections.values().stream()
            .filter(viewer -> !viewer.connectionId().equals(subject.connectionId()))
            .filter(viewer -> isVisible(viewer, subject))
            .toList();
    }

    public Collection<ConnectionInfo> recipientsForMessage(ConnectionInfo sender) {
        return connections.values().stream()
            .filter(viewer -> canSeeMessage(viewer, sender))
            .toList();
    }

    public List<ConnectionInfo> recipientsForLeave(ConnectionInfo leavingConnection) {
        if (isGuest(leavingConnection)) {
            return Collections.emptyList();
        }
        return connections.values().stream()
            .filter(viewer -> !viewer.connectionId().equals(leavingConnection.connectionId()))
            .toList();
    }

    private boolean isVisible(ConnectionInfo viewer, ConnectionInfo subject) {
        return !isGuest(subject);
    }

    private boolean canSeeMessage(ConnectionInfo viewer, ConnectionInfo messageSender) {
        return true;
    }

    public boolean hasUser(UserInfo user) {
        return connections.values().stream()
            .anyMatch(connection -> connection.user().equals(user));  //the equals method is overridden for records
    }

    private boolean isGuest(ConnectionInfo connection) {
        return connection.user().type() == UserType.GUEST;
    }
}
