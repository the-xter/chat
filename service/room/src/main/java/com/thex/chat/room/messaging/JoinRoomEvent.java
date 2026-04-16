package com.thex.chat.room.messaging;

import com.thex.chat.room.dto.ConnectionInfo;

public record JoinRoomEvent(
        ConnectionInfo connectionInfo,
        String roomId
) {
    @Override
    public String toString() {
        return "{" +
            "connectionInfo=" + connectionInfo +
            ", roomId='" + roomId + '\'' +
            '}';
    }
}
