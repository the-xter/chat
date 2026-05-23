package com.thex.chat.chatapi.messaging;

import com.thex.chat.chatapi.dto.UserInfo;

import java.util.Collection;

public record RoomVisitors(
    String targetConnectionId,
    String roomId,
    Collection<UserInfo> visitors
) {
}
