package com.thex.chat.chatapi.messaging;

import com.thex.chat.chatapi.dto.ConnectionInfo;

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
