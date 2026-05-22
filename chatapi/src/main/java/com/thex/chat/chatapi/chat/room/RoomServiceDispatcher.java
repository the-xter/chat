package com.thex.chat.chatapi.chat.room;

import com.thex.chat.chatapi.chat.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RoomServiceDispatcher extends AbstractServiceDispatcher {

    protected RoomServiceDispatcher(RoomService roomService, CallBackInvoker callBackInvoker) {
        super(
            "room",
            callBackInvoker
        );

        addMethodHandler(
            "join",
            (connectionInfo, parameters) -> {
                log.info("RoomServiceDispatcher join {}; {}", connectionInfo, parameters);
                roomService.join(connectionInfo, parameters);
                return CallResult.optional();
            }
        );

        addMethodHandler(
            "leave",
            (connectionInfo, parameters) -> {
                log.info("RoomServiceDispatcher leave {}; {}", connectionInfo, parameters);
                roomService.leave(connectionInfo, parameters);
                return CallResult.optional();
            }
        );

        addMethodHandler(
            "send",
            (connectionInfo, parameters) -> {
                log.trace("RoomServiceDispatcher send {}; {}", connectionInfo, parameters);
                roomService.send(connectionInfo, parameters);
                return CallResult.optional();
            }
        );
    }
}
