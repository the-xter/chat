package com.thex.chat.connection.messaging;

import com.thex.chat.connection.dto.ConnectionInfo;

public record ConnectionEvent(
        String eventType,
        ConnectionInfo connection
) {}
