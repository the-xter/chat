package com.thex.chat.connection.controller;

import com.thex.chat.connection.dto.ConnectionInfo;
import com.thex.chat.connection.service.ConnectionStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/connections")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionStateService connectionStateService;

    @PostMapping
    public void connect(@RequestBody ConnectionInfo connectionInfo) {
        connectionStateService.connect(connectionInfo);
    }

    @DeleteMapping("/{connectionId}")
    public void disconnect(@PathVariable String connectionId) {
        connectionStateService.disconnect(connectionId);
    }
}
