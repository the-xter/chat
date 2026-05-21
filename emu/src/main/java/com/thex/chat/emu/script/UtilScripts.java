package com.thex.chat.emu.script;

import com.thex.chat.emu.performer.PerformerParameters;
import org.springframework.stereotype.Component;

@Component
public class UtilScripts {

    private final Script waitScript = new Script("wait N ms", this::waitPerformer);

    public Script waitNms() {
        return waitScript;
    }

    private void waitPerformer(PerformerParameters params) {
        try {
            int timeout = 1000;
            if (params.options() instanceof Integer) {
                timeout = (Integer)params.options();
            }
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
