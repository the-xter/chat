package com.thex.chat.emu.script;

import com.thex.chat.emu.client.CometdServiceClient;
import com.thex.chat.emu.performer.PerformerParameters;
import com.thex.chat.emu.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomScripts {
    private static final String DEFAULT_ROOM = "general";

    private final CometdServiceClient cometdService;

    private final Script joinScript = new Script("join room", this::joinPerformer);
    private final Script leaveScript = new Script("leave room", this::leavePerformer);

    public Script join() {
        return joinScript;
    }

    public Script leave() {
        return leaveScript;
    }

    private void joinPerformer(PerformerParameters params) {
        User user = params.user();
        cometdService.joinRoom(user.getBayeuxClient(), roomId(params));
    }

    private void leavePerformer(PerformerParameters params) {
        User user = params.user();
        cometdService.leaveRoom(user.getBayeuxClient(), roomId(params));
    }

    private static String roomId(PerformerParameters params) {
        Object options = params.options();
        if (options instanceof String s && !s.isBlank()) {
            return s;
        }
        return DEFAULT_ROOM;
    }
}
