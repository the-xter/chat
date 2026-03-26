package com.thex.chat.visitor.messaging;

public record SessionEvent(
        String sessionId,
        String eventType,
        boolean authenticated,
        String username
) {}
