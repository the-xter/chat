package com.thex.chat.room.messaging;

import java.util.List;

public record RoomUpdate(
        String roomId,
        List<String> members
) {}
