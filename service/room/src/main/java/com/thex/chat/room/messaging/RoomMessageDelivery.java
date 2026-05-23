package com.thex.chat.room.messaging;

import com.thex.chat.room.dto.UserInfo;

import java.time.Instant;
import java.util.List;

public record RoomMessageDelivery(
    List<String> recipients,
    Long messageId,
    String roomId,
    UserInfo sender,
    String text,
    Instant createdAt
) {
    @Override
    public String toString() {
        return "{" +
            "recipients=" + recipients +
            ", messageId=" + messageId +
            ", roomId='" + roomId + '\'' +
            ", sender=" + sender +
            ", text='" + text + '\'' +
            ", createdAt=" + createdAt +
            '}';
    }
}
