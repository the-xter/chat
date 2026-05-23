package com.thex.chat.message.messaging;

import com.thex.chat.message.dto.ConnectionInfo;

public record SendRoomMessageRequest(
    ConnectionInfo connectionInfo,
    String roomId,
    String text
) {
    @Override
    public String toString() {
        return "{" +
            "connectionInfo=" + connectionInfo +
            ", roomId='" + roomId + '\'' +
            ", text='" + text + '\'' +
            '}';
    }
}
