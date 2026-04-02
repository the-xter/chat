package com.thex.chat.connection.dto;

public record ConnectionInfo(
        String connectionId,
        UserInfo user
) {}
