package com.thex.chat.room.messaging;

import com.thex.chat.room.dto.ConnectionInfo;

public record ConnectionEvent(
    String eventType,
    ConnectionInfo connection
) {}