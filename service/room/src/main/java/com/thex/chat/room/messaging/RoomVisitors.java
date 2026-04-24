package com.thex.chat.room.messaging;

import com.thex.chat.room.dto.UserInfo;

import java.util.List;

public record RoomVisitors(
        String targetConnectionId,
        String roomId,
        List<UserInfo> visitors
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
