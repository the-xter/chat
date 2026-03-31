package com.thex.chat.connection.controller;

import com.thex.chat.connection.service.ConnectionStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/connections")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionStateService connectionStateService;

    @PostMapping
    public void connect(@RequestBody ConnectionRequest request) {
        connectionStateService.connect(request.sessionId(), request.authenticated(), request.username());
    }

    @DeleteMapping("/{sessionId}")
    public void disconnect(@PathVariable String sessionId) {
        connectionStateService.disconnect(sessionId);
    }

    public record ConnectionRequest(String sessionId, boolean authenticated, String username) {}
}
