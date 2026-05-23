package com.thex.chat.message.messaging;

import com.thex.chat.message.dto.ConnectionInfo;

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
