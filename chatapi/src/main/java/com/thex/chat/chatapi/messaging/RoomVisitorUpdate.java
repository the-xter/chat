package com.thex.chat.chatapi.messaging;

import com.thex.chat.chatapi.dto.UserInfo;
import lombok.NonNull;

import java.util.List;

public record RoomVisitorUpdate(
    List<String> recipients,
    String roomId,
    UserInfo user,
    Action action
) {
    public enum Action {
        JOINED,
        LEFT
    }

    @NonNull
    @Override
    public String toString() {
        return "{" +
            "for='" + recipients + '\'' +
            ", roomId='" + roomId + '\'' +
            ", user=" + user +
            ", action=" + action +
            '}';
    }
}
