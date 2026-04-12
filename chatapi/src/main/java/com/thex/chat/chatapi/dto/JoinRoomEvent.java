package com.thex.chat.chatapi.dto;

public record JoinRoomEvent (
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
