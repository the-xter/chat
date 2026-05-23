package com.thex.chat.room.messaging;

import com.thex.chat.room.dto.ConnectionInfo;

import java.time.Instant;

public record RoomMessageEvent(
    Long messageId,
    String roomId,
    Instant createdAt,
    ConnectionInfo connectionInfo,
    String text
) {
    @Override
    public String toString() {
        return "{" +
            "messageId=" + messageId +
            ", roomId='" + roomId + '\'' +
            ", createdAt=" + createdAt +
            ", connectionInfo=" + connectionInfo +
            ", text='" + text + '\'' +
            '}';
    }
}
