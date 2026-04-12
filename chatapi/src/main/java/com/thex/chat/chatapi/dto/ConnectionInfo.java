package com.thex.chat.chatapi.dto;

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
