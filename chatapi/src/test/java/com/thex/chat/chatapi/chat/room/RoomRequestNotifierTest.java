package com.thex.chat.chatapi.chat.room;

import com.thex.chat.chatapi.config.RabbitConfig;
import com.thex.chat.chatapi.dto.ConnectionInfo;
import com.thex.chat.chatapi.dto.UserInfo;
import com.thex.chat.chatapi.dto.UserType;
import com.thex.chat.chatapi.messaging.JoinRoomRequest;
import com.thex.chat.chatapi.messaging.LeaveRoomRequest;
import com.thex.chat.chatapi.messaging.SendRoomMessageRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class RoomRequestNotifierTest {

    private static final ConnectionInfo CONNECTION =
        new ConnectionInfo("conn-1", new UserInfo("42", "alice", UserType.REGISTERED));

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RoomRequestNotifier notifier;

    @Test
    void notifyJoin_sendsToChatEventsExchange_withJoinRoutingKey() {
        JoinRoomRequest request = new JoinRoomRequest(CONNECTION, "lobby");

        notifier.notifyJoin(request);

        verify(rabbitTemplate).convertAndSend(RabbitConfig.EXCHANGE, "room.join.request", request);
        verifyNoMoreInteractions(rabbitTemplate);
    }

    @Test
    void notifyLeave_sendsToChatEventsExchange_withLeaveRoutingKey() {
        LeaveRoomRequest request = new LeaveRoomRequest(CONNECTION, "lobby");

        notifier.notifyLeave(request);

        verify(rabbitTemplate).convertAndSend(RabbitConfig.EXCHANGE, "room.leave.request", request);
        verifyNoMoreInteractions(rabbitTemplate);
    }

    @Test
    void notifySend_sendsToChatEventsExchange_withSendMessageRoutingKey() {
        SendRoomMessageRequest request = new SendRoomMessageRequest(CONNECTION, "lobby", "hello");

        notifier.notifySendMessage(request);

        verify(rabbitTemplate).convertAndSend(RabbitConfig.EXCHANGE, "room.message.send", request);
        verifyNoMoreInteractions(rabbitTemplate);
    }
}
