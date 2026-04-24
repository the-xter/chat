package com.thex.chat.chatapi.chat.room;

import com.thex.chat.chatapi.chat.CallException;
import com.thex.chat.chatapi.chat.ParametersWrapper;
import com.thex.chat.chatapi.dto.ConnectionInfo;
import com.thex.chat.chatapi.messaging.JoinRoomRequest;
import com.thex.chat.chatapi.messaging.LeaveRoomRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomService {
    private static final String ROOM_ID = "roomId";

    private final RoomRequestNotifier notifier;

    public void join(ConnectionInfo connectionInfo, ParametersWrapper parameters) throws CallException {
        notifier.notifyJoin(new JoinRoomRequest(connectionInfo, getRoom(parameters)));
    }

    public void leave(ConnectionInfo connectionInfo, ParametersWrapper parameters) throws CallException {
        notifier.notifyLeave(new LeaveRoomRequest(connectionInfo, getRoom(parameters)));
    }

    private String getRoom(ParametersWrapper parameters) throws CallException {
        String roomId = parameters.getString(ROOM_ID);
        if (roomId == null) {
            throw new CallException("error.room.null");
        }
        return roomId;
    }
}
