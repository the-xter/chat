package com.thex.chat.emu.script;

import com.thex.chat.emu.performer.PerformerParameters;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class UtilScripts {

    private final Random rand = new Random();
    private final Script waitMsScript = new Script("wait N ms", this::waitPerformerMs);
    private final Script waitSecScript = new Script("wait N s", this::waitPerformerS);

    public Script waitNms() {
        return waitMsScript;
    }

    public Script waitNs() {
        return waitSecScript;
    }

    private void waitPerformerS(PerformerParameters params) {
        waitDeviatedMs(timeout(params, 1) * 1000);
    }

    private void waitPerformerMs(PerformerParameters params) {
        waitDeviatedMs(timeout(params, 1000));
    }

    private void waitDeviatedMs(int timeout) {
        int deviation = rand.nextInt(timeout / 10);
        int deviationSign = rand.nextBoolean() ? 1 : -1;
        waitMs(timeout + (deviation * deviationSign));
    }

    private void waitMs(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private int timeout(PerformerParameters params, int defaultValue) {
        int timeout = defaultValue;
        if (params.options() instanceof Integer) {
            timeout = (Integer)params.options();
        }
        return timeout;
    }
}
