package com.thex.chat.chatapi.messaging;

import com.thex.chat.chatapi.dto.ConnectionInfo;

public record ConnectionEvent(
        String eventType,
        ConnectionInfo connection
) {}
