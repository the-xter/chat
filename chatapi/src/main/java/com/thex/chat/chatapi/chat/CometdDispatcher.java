package com.thex.chat.chatapi.chat;

import com.thex.chat.chatapi.chat.room.RoomServiceDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.cometd.annotation.Listener;
import org.cometd.annotation.Service;
import org.cometd.bayeux.server.*;

@Slf4j
@jakarta.inject.Named // Tells Spring that this is a bean
@jakarta.inject.Singleton // Tells Spring that this is a singleton
@Service
public class CometdDispatcher {

    private final RoomServiceDispatcher roomServiceDispatcher;

    public CometdDispatcher(RoomServiceDispatcher roomServiceDispatcher) {
        this.roomServiceDispatcher = roomServiceDispatcher;
    }

    @Listener("/service/room")
    public void roomServiceCall(ServerSession client, ServerMessage.Mutable message) {
        roomServiceDispatcher.dispatch(client, message);
    }
}
