package com.thex.chat.emu.script;

import com.thex.chat.emu.client.AuthServiceClient;
import com.thex.chat.emu.client.CometdServiceClient;
import com.thex.chat.emu.performer.PerformerParameters;
import com.thex.chat.emu.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionScripts {

    private final AuthServiceClient authService;
    private final CometdServiceClient cometdService;

    private final Script connectScript = new Script("connect", this::connectPerformer);
    private final Script disconnectScript = new Script("disconnect", this::disconnectPerformer);

    public Script connect() {
        return connectScript;
    }

    public Script disconnect() {
        return disconnectScript;
    }

    private void connectPerformer(PerformerParameters params) {
        User user = params.user();
        user.setToken(authService.login(user.getName(), user.getPassword()));
        user.setBayeuxClient(cometdService.connect(user.getName(), user.getToken()));
    }

    private void disconnectPerformer(PerformerParameters params) {
        User user = params.user();
        cometdService.disconnect(user.getBayeuxClient());
        user.setBayeuxClient(null);
    }
}
