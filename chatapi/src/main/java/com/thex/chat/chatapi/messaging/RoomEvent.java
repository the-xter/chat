package com.thex.chat.chatapi.messaging;

public record RoomEvent(
        String sessionId,
        String action,
        String roomId
) {}
