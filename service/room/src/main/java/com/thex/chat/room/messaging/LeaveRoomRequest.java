package com.thex.chat.room.messaging;

import com.thex.chat.room.dto.ConnectionInfo;

public record LeaveRoomRequest(
        ConnectionInfo connectionInfo,
        String roomId
) {}

