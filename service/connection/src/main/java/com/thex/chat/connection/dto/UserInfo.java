package com.thex.chat.connection.dto;

public record UserInfo(
        String id,
        String name,
        UserType type
) {}
