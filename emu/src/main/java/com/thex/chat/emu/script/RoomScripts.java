package com.thex.chat.emu.script;

import com.thex.chat.emu.performer.PerformerParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomScripts {
    private final Script joinScript = new Script("join room", this::joinPerformer);
    private final Script leaveScript = new Script("leave room", this::leavePerformer);

    public Script join() {
        return joinScript;
    }

    public Script leave() {
        return leaveScript;
    }

    private void joinPerformer(PerformerParameters params) {
    }

    private void leavePerformer(PerformerParameters params) {
    }
}
