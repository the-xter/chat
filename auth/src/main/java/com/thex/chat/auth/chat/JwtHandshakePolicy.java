package com.thex.chat.auth.chat;

import com.thex.chat.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.cometd.bayeux.server.*;
import org.cometd.server.DefaultSecurityPolicy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakePolicy extends DefaultSecurityPolicy {

    public static final String SESSION_ATTR_USERNAME = "username";
    public static final String SESSION_ATTR_AUTHENTICATED = "authenticated";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void canHandshake(BayeuxServer server,
                             ServerSession session,
                             ServerMessage message,
                             Promise<Boolean> promise) {
        if (session.isLocalSession()) {
            promise.succeed(true);
            return;
        }

        Map<String, Object> ext = message.getExt();
        String token = null;
        if (ext != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> auth = (Map<String, Object>) ext.get("auth");
            if (auth != null) {
                token = (String) auth.get("token");
            }
        }

        if (token == null || token.isBlank()) {
            session.setAttribute(SESSION_ATTR_AUTHENTICATED, false);
            session.setAttribute(SESSION_ATTR_USERNAME, null);
            promise.succeed(true);
            return;
        }

        if (jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            session.setAttribute(SESSION_ATTR_AUTHENTICATED, true);
            session.setAttribute(SESSION_ATTR_USERNAME, username);
            promise.succeed(true);
        } else {
            ServerMessage.Mutable reply = message.getAssociated();
            Map<String, Object> advice = reply.getAdvice(true);
            advice.put("reconnect", "none");
            reply.put("error", "401::Invalid authentication token");
            promise.succeed(false);
        }
    }
}
