package com.thex.chat.room.messaging;

public record SessionEvent(
        String sessionId,
        String eventType,
        boolean authenticated,
        String username
) {}
