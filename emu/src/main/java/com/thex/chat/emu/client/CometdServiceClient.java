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

    public CometdServiceClient(@Value("${emu.cometd-url}") String cometdUrl) {
        this.cometdUrl = cometdUrl;
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
                //TODO repeats on handshake failure (cometD tries to reconnect)
                log.error("CometD handshake failed for {}: {}", username, message);
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
}
