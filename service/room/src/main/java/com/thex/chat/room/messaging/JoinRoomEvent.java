package com.thex.chat.room.messaging;

import com.thex.chat.room.dto.ConnectionInfo;

public record JoinRoomEvent(
        ConnectionInfo connectionInfo,
        String roomId
) {}
