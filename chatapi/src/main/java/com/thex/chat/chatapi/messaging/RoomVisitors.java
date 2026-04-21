package com.thex.chat.chatapi.messaging;

import com.thex.chat.chatapi.dto.UserInfo;

import java.util.List;

public record RoomVisitors(
        String targetConnectionId,
        String roomId,
        List<UserInfo> visitors
) {}
