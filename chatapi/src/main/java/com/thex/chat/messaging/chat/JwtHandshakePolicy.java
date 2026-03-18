package com.thex.chat.messaging.chat;

import com.thex.chat.messaging.security.JwtTokenValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cometd.bayeux.Promise;
import org.cometd.bayeux.server.*;
import org.cometd.server.DefaultSecurityPolicy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtHandshakePolicy extends DefaultSecurityPolicy {
    public static final String SESSION_ATTR_USERNAME = "username";
    public static final String SESSION_ATTR_AUTHENTICATED = "authenticated";

    private final JwtTokenValidator jwtTokenValidator;

    @Override
    public boolean canHandshake(BayeuxServer server, ServerSession session, ServerMessage message) {
        if (session.isLocalSession()) {
            return true;
        }

        String token = null;
        @SuppressWarnings("unchecked")
        Map<String, Object> auth = (Map<String, Object>) message.get("auth");
        if (auth != null) {
            token = (String) auth.get("token");
        }

        if (token == null || token.isBlank()) {
            session.setAttribute(SESSION_ATTR_AUTHENTICATED, false);
            session.setAttribute(SESSION_ATTR_USERNAME, null);
            return true;
        }

        if (jwtTokenValidator.validateToken(token)) {
            String username = jwtTokenValidator.getUsernameFromToken(token);
            session.setAttribute(SESSION_ATTR_AUTHENTICATED, true);
            session.setAttribute(SESSION_ATTR_USERNAME, username);
            return true;
        } else {
            ServerMessage.Mutable reply = message.getAssociated();
            Map<String, Object> advice = reply.getAdvice(true);
            advice.put("reconnect", "none");
            reply.put("error", "401::Invalid authentication token");
            return false;
        }
    }
}
