package com.thex.chat.message.service;

import com.thex.chat.message.dto.ConnectionInfo;
import com.thex.chat.message.dto.UserInfo;
import com.thex.chat.message.messaging.SendRoomMessageRequest;
import com.thex.chat.message.model.RoomMessage;
import com.thex.chat.message.repository.RoomMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final RoomMessageRepository repository;

    public RoomMessage store(SendRoomMessageRequest request) {
        ConnectionInfo connection = request.connectionInfo();
        UserInfo user = connection != null ? connection.user() : null;

        RoomMessage entity = RoomMessage.builder()
            .roomId(request.roomId())
            .senderId(user != null ? user.id() : null)
            .senderName(user != null ? user.name() : null)
            .text(request.text())
            .build();

        RoomMessage saved = repository.save(entity);
        log.info("Stored room message {}", request);
        return saved;
    }
}
