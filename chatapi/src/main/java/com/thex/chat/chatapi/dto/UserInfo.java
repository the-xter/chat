package com.thex.chat.chatapi.dto;

public record UserInfo(
        String id,
        String name,
        UserType type
) {}
