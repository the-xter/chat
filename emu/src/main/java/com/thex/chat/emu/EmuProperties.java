package com.thex.chat.emu;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "emu")
public record EmuProperties(
    String authUrl,
    String cometdUrl,
    List<EmuUser> users
) {
    public record EmuUser(String name, String password) {
    }
}
