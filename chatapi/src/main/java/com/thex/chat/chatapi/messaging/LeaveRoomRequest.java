package com.thex.chat.chatapi.messaging;

import com.thex.chat.chatapi.dto.ConnectionInfo;

public record LeaveRoomRequest(
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

