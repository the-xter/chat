package com.thex.chat.message.dto;

public record ConnectionInfo(
    String connectionId,
    UserInfo user
) {
    @Override
    public String toString() {
        return "{" +
            "connectionId='" + connectionId + '\'' +
            ", user=" + user +
            '}';
    }
}
