package com.thex.chat.chatapi.chat;

import com.thex.chat.chatapi.dto.ConnectionInfo;
import com.thex.chat.chatapi.dto.UserInfo;
import com.thex.chat.chatapi.dto.UserType;
import com.thex.chat.chatapi.security.JwtTokenValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cometd.bayeux.server.*;
import org.cometd.server.DefaultSecurityPolicy;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Role(BeanDefinition.ROLE_INFRASTRUCTURE)  //to avoid post-processing warning
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtHandshakePolicy extends DefaultSecurityPolicy {

    private final AtomicInteger guestCounter = new AtomicInteger();

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

        ConnectionInfo connectionInfo;
        if (token == null || token.isBlank()) {
            connectionInfo = new ConnectionInfo(session.getId(), generateGuest());
        } else if (jwtTokenValidator.validateToken(token)) {
            connectionInfo = new ConnectionInfo(session.getId(), jwtTokenValidator.getUserFromToken(token));
        } else {
            ServerMessage.Mutable reply = message.getAssociated();
            Map<String, Object> advice = reply.getAdvice(true);
            advice.put("reconnect", "none");
            reply.put("error", "401::Invalid authentication token");
            return false;
        }

        session.setAttribute(Consts.CONNECTION_INFO, connectionInfo);
        return true;
    }

    private UserInfo generateGuest() {
        return new UserInfo(
            null,
            "guest-" + guestCounter.incrementAndGet(),
            UserType.GUEST
        );
    }
}
