package com.thex.chat.emu.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cometd.client.BayeuxClient;

@RequiredArgsConstructor
public class User {
    @Getter
    private final String name;
    @Getter
    private final String password;

    @Getter
    @Setter
    private String token;

    @Getter
    @Setter
    private BayeuxClient bayeuxClient;

    @Override
    public String toString() {
        return "User{" +
            "name='" + name + '\'' +
            '}';
    }
}
