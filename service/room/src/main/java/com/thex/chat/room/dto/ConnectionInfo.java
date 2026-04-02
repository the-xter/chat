package com.thex.chat.room.dto;

public record ConnectionInfo(
        String connectionId,
        UserInfo user
) {}
