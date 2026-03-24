package com.thex.chat.chatapi.messaging;

public record SessionEvent(
        String sessionId,
        String eventType,
        boolean authenticated,
        String username
) {}
