package com.thex.chat.chatapi.chat.room;

import com.thex.chat.chatapi.chat.CallException;
import com.thex.chat.chatapi.chat.ParametersWrapper;
import com.thex.chat.chatapi.dto.ConnectionInfo;
import com.thex.chat.chatapi.dto.UserInfo;
import com.thex.chat.chatapi.dto.UserType;
import com.thex.chat.chatapi.messaging.JoinRoomRequest;
import com.thex.chat.chatapi.messaging.LeaveRoomRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    private static final ConnectionInfo CONNECTION =
        new ConnectionInfo("conn-1", new UserInfo("42", "alice", UserType.REGISTERED));

    @Mock
    private RoomRequestNotifier notifier;

    @InjectMocks
    private RoomService service;

    @Test
    void join_publishesJoinRequestForRoom() throws Exception {
        service.join(CONNECTION, params("roomId", "lobby"));

        verify(notifier).notifyJoin(new JoinRoomRequest(CONNECTION, "lobby"));
    }

    @Test
    void leave_publishesLeaveRequestForRoom() throws Exception {
        service.leave(CONNECTION, params("roomId", "lobby"));

        verify(notifier).notifyLeave(new LeaveRoomRequest(CONNECTION, "lobby"));
    }

    @Test
    void join_throws_whenRoomIdMissing() {
        assertThatThrownBy(() -> service.join(CONNECTION, params()))
            .isInstanceOf(CallException.class)
            .hasMessage("error.room.null");
        verifyNoInteractions(notifier);
    }

    @Test
    void leave_throws_whenRoomIdMissing() {
        assertThatThrownBy(() -> service.leave(CONNECTION, params()))
            .isInstanceOf(CallException.class)
            .hasMessage("error.room.null");
        verifyNoInteractions(notifier);
    }

    @Test
    void join_throws_whenRoomIdIsExplicitNull() {
        Map<String, Object> raw = new HashMap<>();
        raw.put("roomId", null);

        assertThatThrownBy(() -> service.join(CONNECTION, ParametersWrapper.wrap(raw)))
            .isInstanceOf(CallException.class)
            .hasMessage("error.room.null");
    }

    private static ParametersWrapper params(String key, Object value) {
        Map<String, Object> raw = new HashMap<>();
        raw.put(key, value);
        return ParametersWrapper.wrap(raw);
    }

    private static ParametersWrapper params() {
        return ParametersWrapper.wrap(new HashMap<>());
    }
}
