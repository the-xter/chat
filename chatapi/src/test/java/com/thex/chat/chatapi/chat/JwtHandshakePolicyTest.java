package com.thex.chat.chatapi.chat;

import com.thex.chat.chatapi.dto.ConnectionInfo;
import com.thex.chat.chatapi.dto.UserInfo;
import com.thex.chat.chatapi.dto.UserType;
import com.thex.chat.chatapi.security.JwtTokenValidator;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtHandshakePolicyTest {

    private static final String VALID_TOKEN = "valid.token";
    private static final String INVALID_TOKEN = "bogus.token";

    @Mock
    private JwtTokenValidator validator;
    @Mock
    private BayeuxServer server;
    @Mock
    private ServerSession session;
    @Mock
    private ServerMessage message;
    @Mock
    private ServerMessage.Mutable reply;

    private JwtHandshakePolicy policy;

    @BeforeEach
    void setUp() {
        policy = new JwtHandshakePolicy(validator);
    }

    @Test
    void localSession_isAlwaysAllowed_andNoConnectionInfoSet() {
        when(session.isLocalSession()).thenReturn(true);

        boolean allowed = policy.canHandshake(server, session, message);

        assertThat(allowed).isTrue();
        verify(session, never()).setAttribute(eq(Consts.CONNECTION_INFO), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void missingAuth_attachesGuestConnectionInfo() {
        when(session.isLocalSession()).thenReturn(false);
        when(session.getId()).thenReturn("session-1");
        when(message.get("auth")).thenReturn(null);

        boolean allowed = policy.canHandshake(server, session, message);

        assertThat(allowed).isTrue();
        ConnectionInfo captured = captureSetConnectionInfo();
        assertThat(captured.connectionId()).isEqualTo("session-1");
        assertThat(captured.user().type()).isEqualTo(UserType.GUEST);
        assertThat(captured.user().id()).isNull();
        assertThat(captured.user().name()).isEqualTo("guest-1");
    }

    @Test
    void blankToken_attachesGuestConnectionInfo() {
        when(session.isLocalSession()).thenReturn(false);
        when(session.getId()).thenReturn("session-2");
        when(message.get("auth")).thenReturn(authMap("   "));

        boolean allowed = policy.canHandshake(server, session, message);

        assertThat(allowed).isTrue();
        assertThat(captureSetConnectionInfo().user().type()).isEqualTo(UserType.GUEST);
    }

    @Test
    void guestCounter_incrementsAcrossHandshakes() {
        when(session.isLocalSession()).thenReturn(false);
        when(session.getId()).thenReturn("any");
        when(message.get("auth")).thenReturn(null);

        policy.canHandshake(server, session, message);
        policy.canHandshake(server, session, message);
        policy.canHandshake(server, session, message);

        ArgumentCaptor<Object> attr = ArgumentCaptor.forClass(Object.class);
        verify(session, org.mockito.Mockito.times(3))
            .setAttribute(eq(Consts.CONNECTION_INFO), attr.capture());
        assertThat(attr.getAllValues())
            .extracting(o -> ((ConnectionInfo) o).user().name())
            .containsExactly("guest-1", "guest-2", "guest-3");
    }

    @Test
    void validToken_attachesRegisteredUser() {
        UserInfo registered = new UserInfo("42", "alice", UserType.REGISTERED);
        when(session.isLocalSession()).thenReturn(false);
        when(session.getId()).thenReturn("session-3");
        when(message.get("auth")).thenReturn(authMap(VALID_TOKEN));
        when(validator.validateToken(VALID_TOKEN)).thenReturn(true);
        when(validator.getUserFromToken(VALID_TOKEN)).thenReturn(registered);

        boolean allowed = policy.canHandshake(server, session, message);

        assertThat(allowed).isTrue();
        assertThat(captureSetConnectionInfo())
            .isEqualTo(new ConnectionInfo("session-3", registered));
    }

    @Test
    void invalidToken_isRejected_andSetsReconnectNoneAdvice() {
        when(session.isLocalSession()).thenReturn(false);
        when(message.get("auth")).thenReturn(authMap(INVALID_TOKEN));
        when(validator.validateToken(INVALID_TOKEN)).thenReturn(false);
        Map<String, Object> advice = new HashMap<>();
        when(message.getAssociated()).thenReturn(reply);
        when(reply.getAdvice(true)).thenReturn(advice);

        boolean allowed = policy.canHandshake(server, session, message);

        assertThat(allowed).isFalse();
        assertThat(advice).containsEntry("reconnect", "none");
        verify(reply).put(eq("error"), eq("401::Invalid authentication token"));
        verify(session, never()).setAttribute(eq(Consts.CONNECTION_INFO), org.mockito.ArgumentMatchers.any());
    }

    private ConnectionInfo captureSetConnectionInfo() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(session).setAttribute(eq(Consts.CONNECTION_INFO), captor.capture());
        return (ConnectionInfo) captor.getValue();
    }

    private static Map<String, Object> authMap(String token) {
        Map<String, Object> auth = new HashMap<>();
        auth.put("token", token);
        return auth;
    }
}
