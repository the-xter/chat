package com.thex.chat.room.messaging;

import com.thex.chat.room.config.RabbitConfig;
import com.thex.chat.room.dto.ConnectionInfo;
import com.thex.chat.room.dto.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomNotifier {
    private final RabbitTemplate rabbitTemplate;

    public void sendRoomVisitors(String connectionId, String roomId, Collection<UserInfo> visitors) {
        RoomVisitors roomVisitors = new RoomVisitors(connectionId, roomId, visitors);
        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE,
            "room.visitors",
            roomVisitors
        );
        log.info("Published room.visitors {}", roomVisitors);
    }

    public void sendRoomVisitorUpdate(RoomVisitorUpdate update) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "room.visitor.update", update);
        log.info("Published room.visitor.update {}", update);
    }

    public void notifyVisitorJoined(Collection<ConnectionInfo> recipients, String roomId, UserInfo user) {
        sendRoomVisitorUpdate(
            new RoomVisitorUpdate(
                recipients.stream().map(ConnectionInfo::connectionId).toList(),
                roomId,
                user,
                RoomVisitorUpdate.Action.JOINED
            )
        );
    }

    public void notifyVisitorLeft(Collection<ConnectionInfo> recipients, String roomId, UserInfo user) {
        sendRoomVisitorUpdate(
            new RoomVisitorUpdate(
                recipients.stream().map(ConnectionInfo::connectionId).toList(),
                roomId,
                user,
                RoomVisitorUpdate.Action.LEFT
            )
        );
    }
}
