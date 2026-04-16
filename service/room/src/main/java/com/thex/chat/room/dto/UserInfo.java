package com.thex.chat.room.dto;

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
