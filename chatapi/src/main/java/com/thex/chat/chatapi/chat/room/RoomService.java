package com.thex.chat.chatapi.chat.room;

import com.thex.chat.chatapi.chat.CallException;
import com.thex.chat.chatapi.chat.ParametersWrapper;
import com.thex.chat.chatapi.dto.ConnectionInfo;
import com.thex.chat.chatapi.dto.UserInfo;
import com.thex.chat.chatapi.dto.UserType;
import com.thex.chat.chatapi.messaging.JoinRoomRequest;
import com.thex.chat.chatapi.messaging.LeaveRoomRequest;
import com.thex.chat.chatapi.messaging.SendRoomMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {
    private static final String ROOM_ID = "roomId";
    private static final String TEXT = "text";

    static final String ERROR_ROOM_NULL = "error.room.null";
    static final String ERROR_GUEST_NOT_ALLOWED = "error.chat.guest.not.allowed";
    static final String ERROR_TEXT_EMPTY = "error.chat.text.empty";

    private final RoomRequestNotifier notifier;

    public void join(ConnectionInfo connectionInfo, ParametersWrapper parameters) throws CallException {
        notifier.notifyJoin(new JoinRoomRequest(connectionInfo, getRoom(parameters)));
    }

    public void leave(ConnectionInfo connectionInfo, ParametersWrapper parameters) throws CallException {
        notifier.notifyLeave(new LeaveRoomRequest(connectionInfo, getRoom(parameters)));
    }

    public void sendMessage(ConnectionInfo connectionInfo, ParametersWrapper parameters) throws CallException {
        UserInfo user = connectionInfo.user();
        if (user == null || user.type() != UserType.REGISTERED) {
            throw new CallException(ERROR_GUEST_NOT_ALLOWED);
        }

        String roomId = getRoom(parameters);

        String text = parameters.getString(TEXT);
        if (text == null || text.isBlank()) {
            throw new CallException(ERROR_TEXT_EMPTY);
        }

        notifier.notifySendMessage(new SendRoomMessageRequest(connectionInfo, roomId, text));
    }

    private String getRoom(ParametersWrapper parameters) throws CallException {
        String roomId = parameters.getString(ROOM_ID);
        if (roomId == null || roomId.isBlank()) {
            throw new CallException(ERROR_ROOM_NULL);
        }
        return roomId;
    }
}
