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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    private static final ConnectionInfo CONNECTION =
        new ConnectionInfo("conn-1", new UserInfo("42", "alice", UserType.REGISTERED));
    private static final ConnectionInfo GUEST_CONNECTION =
        new ConnectionInfo("conn-2", new UserInfo(null, "guest-1", UserType.GUEST));

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

    @Test
    void send_allows_registeredUserWithText() {
        assertThatCode(() -> service.send(CONNECTION, params("roomId", "lobby", "text", "hello")))
            .doesNotThrowAnyException();
        verifyNoInteractions(notifier);
    }

    @Test
    void send_throws_forGuest() {
        assertThatThrownBy(() -> service.send(GUEST_CONNECTION, params("roomId", "lobby", "text", "hello")))
            .isInstanceOf(CallException.class)
            .hasMessage(RoomService.ERROR_GUEST_NOT_ALLOWED);
        verifyNoInteractions(notifier);
    }

    @Test
    void send_throws_whenRoomIdMissing() {
        assertThatThrownBy(() -> service.send(CONNECTION, params("text", "hello")))
            .isInstanceOf(CallException.class)
            .hasMessage(RoomService.ERROR_ROOM_NULL);
    }

    @Test
    void send_throws_whenTextMissing() {
        assertThatThrownBy(() -> service.send(CONNECTION, params("roomId", "lobby")))
            .isInstanceOf(CallException.class)
            .hasMessage(RoomService.ERROR_TEXT_EMPTY);
    }

    @Test
    void send_throws_whenTextBlank() {
        assertThatThrownBy(() -> service.send(CONNECTION, params("roomId", "lobby", "text", "   ")))
            .isInstanceOf(CallException.class)
            .hasMessage(RoomService.ERROR_TEXT_EMPTY);
    }

    private static ParametersWrapper params(String key, Object value) {
        Map<String, Object> raw = new HashMap<>();
        raw.put(key, value);
        return ParametersWrapper.wrap(raw);
    }

    private static ParametersWrapper params(Object... keyValues) {
        Map<String, Object> raw = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            raw.put((String) keyValues[i], keyValues[i + 1]);
        }
        return ParametersWrapper.wrap(raw);
    }

    private static ParametersWrapper params() {
        return ParametersWrapper.wrap(new HashMap<>());
    }
}
