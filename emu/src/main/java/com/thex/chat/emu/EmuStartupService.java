package com.thex.chat.emu;

import jakarta.annotation.PreDestroy;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cometd.client.BayeuxClient;
import org.cometd.client.websocket.jakarta.WebSocketTransport;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmuStartupService {

    private final EmuProperties properties;
    private final List<BayeuxClient> clients = new ArrayList<>();

    @EventListener(ApplicationReadyEvent.class)
    public void connectAll() {
        List<EmuProperties.EmuUser> users = properties.users();
        if (users == null || users.isEmpty()) {
            log.info("No emulator users configured");
            return;
        }

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        RestClient authClient = RestClient.builder().baseUrl(properties.authUrl()).build();

        for (EmuProperties.EmuUser user : users) {
            try {
                String token = login(authClient, user);
                clients.add(connect(container, user.name(), token));
            } catch (Exception e) {
                log.error("Failed to connect emulator user {}: {}", user.name(), e.getMessage());
            }
        }
    }

    @PreDestroy
    public void disconnectAll() {
        for (BayeuxClient client : clients) {
            try {
                client.disconnect();
            } catch (Exception e) {
                log.warn("Disconnect failed: {}", e.getMessage());
            }
        }
    }

    private String login(RestClient client, EmuProperties.EmuUser user) {
        AuthResponse response = client.post()
            .uri("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(new LoginRequest(user.name(), user.password()))
            .retrieve()
            .body(AuthResponse.class);
        if (response == null || response.token() == null) {
            throw new IllegalStateException("No token returned for " + user.name());
        }
        return response.token();
    }

    private BayeuxClient connect(WebSocketContainer container, String name, String token) {
        WebSocketTransport transport = new WebSocketTransport(null, null, container);
        BayeuxClient client = new BayeuxClient(properties.cometdUrl(), transport);
        Map<String, Object> handshakeFields = Map.of("auth", Map.of("token", token));
        client.handshake(handshakeFields, message -> {
            if (message.isSuccessful()) {
                log.info("CometD handshake successful for {}", name);
            } else {
                log.error("CometD handshake failed for {}: {}", name, message);
            }
        });
        return client;
    }

    private record LoginRequest(String username, String password) {
    }

    private record AuthResponse(String token) {
    }
}
