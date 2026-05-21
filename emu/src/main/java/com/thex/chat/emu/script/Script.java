package com.thex.chat.emu.script;

import com.thex.chat.emu.performer.PerformerParameters;
import com.thex.chat.emu.performer.ScriptPerformer;
import com.thex.chat.emu.user.User;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class Script {
    private final String name;
    private final ScriptPerformer performer;

    private final List<ScriptWrapper> scripts = new LinkedList<>();

    public Script(String name) {
        this(name, null);
    }

    public Script(String name, ScriptPerformer performer) {
        this.name = name;
        this.performer = performer;
    }

    public Script add(Script script) {
        return add(script, null);
    }

    public Script add(Script script, Object options) {
        scripts.add(new ScriptWrapper(script, options));
        return this;
    }

    public void perform(User user, int runId) {
        perform(user, runId, null);
    }

    private void perform(User user, int runId,  Object options) {
        for (ScriptWrapper w : scripts) {
            w.script().perform(user, runId, w.options);
        }
        if (null != performer) {
            PerformerParameters parameters = new PerformerParameters(user, runId, options);
            log.info("Run script '{}' with params {}", name, parameters);
            performer.perform(parameters);
        }
    }

    @Override
    public String toString() {
        return "{" +
            "name='" + name + '\'' +
            '}';
    }

    private record ScriptWrapper(
        Script script,
        Object options
    ) {}
}
