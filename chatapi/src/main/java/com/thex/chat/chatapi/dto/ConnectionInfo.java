package com.thex.chat.chatapi.dto;

public record ConnectionInfo(
        String connectionId,
        UserInfo user
) {}
