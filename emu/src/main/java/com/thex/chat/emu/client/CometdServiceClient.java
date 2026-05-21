package com.thex.chat.emu.client;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
import lombok.extern.slf4j.Slf4j;
import org.cometd.client.BayeuxClient;
import org.cometd.client.websocket.jakarta.WebSocketTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Component
public class CometdServiceClient {

    private final String cometdUrl;
    private final String roomServiceChannel;

    public CometdServiceClient(
        @Value("${emu.cometd-url}") String cometdUrl,
        @Value("${emu.room-service-channel:/service/room}") String roomServiceChannel
    ) {
        this.cometdUrl = cometdUrl;
        this.roomServiceChannel = roomServiceChannel;
    }

    public BayeuxClient connect(String username, String token) {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        WebSocketTransport transport = new WebSocketTransport(null, null, container);
        BayeuxClient client = new BayeuxClient(cometdUrl, transport);
        Map<String, Object> handshakeFields = Map.of("auth", Map.of("token", token));
        CompletableFuture<BayeuxClient> future = new CompletableFuture<>();
        client.handshake(handshakeFields, message -> {
            if (message.isSuccessful()) {
                log.info("CometD handshake successful for {}", username);
                future.complete(client);
            } else {
                log.error("CometD handshake failed for {}: {}", username, message);
                // Stop CometD from retrying the handshake.
                client.abort();
                future.complete(null);
            }
        });
        BayeuxClient result = await(future);
        if (result == null) {
            throw new RuntimeException("Unable to get BayeuxClient for " + username);
        }
        return result;
    }

    private BayeuxClient await(CompletableFuture<BayeuxClient> future) {
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect(BayeuxClient bayeuxClient) {
        bayeuxClient.disconnect();
    }

    public void joinRoom(BayeuxClient bayeuxClient, String roomId) {
        log.info("Joining room {}", roomId);
        bayeuxClient.getChannel(roomServiceChannel)
            .publish(Map.of("action", "join", "roomId", roomId));
    }

    public void leaveRoom(BayeuxClient bayeuxClient, String roomId) {
        log.info("Leaving room {}", roomId);
        bayeuxClient.getChannel(roomServiceChannel)
            .publish(Map.of("action", "leave", "roomId", roomId));
    }
}
