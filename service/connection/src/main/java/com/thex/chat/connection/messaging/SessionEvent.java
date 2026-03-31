package com.thex.chat.connection.messaging;

public record SessionEvent(
        String sessionId,
        String eventType,
        boolean authenticated,
        String username
) {}
