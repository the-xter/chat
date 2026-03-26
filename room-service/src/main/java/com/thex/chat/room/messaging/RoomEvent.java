package com.thex.chat.room.messaging;

public record RoomEvent(
        String sessionId,
        String action,
        String roomId
) {}
