package com.thex.chat.room.dto;

public record UserInfo(
        String id,
        String name,
        UserType type
) {}
