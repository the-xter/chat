package com.thex.chat.room.messaging;

import com.thex.chat.room.dto.UserInfo;

import java.util.Collection;

public record RoomVisitors(
        String targetConnectionId,
        String roomId,
        Collection<UserInfo> visitors
) {
    @Override
    public String toString() {
        return "{" +
            "for='" + targetConnectionId + '\'' +
            ", roomId='" + roomId + '\'' +
            ", visitors=" + visitors.size() +
            '}';
    }
}
