package com.thex.chat.chatapi.dto;

public record UserInfo(
    String id,
    String name,
    UserType type
) {

    @Override
    public String toString() {
        return "{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", type=" + type +
            '}';
    }
}
