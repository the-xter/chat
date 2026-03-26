package com.thex.chat.chatapi.messaging;

import java.util.List;

public record RoomUpdate(
        String roomId,
        List<String> members
) {}
