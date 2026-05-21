package com.thex.chat.emu.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthServiceClient {

    private final String authUrl;

    public AuthServiceClient(@Value("${emu.auth-url}") String authUrl) {
        this.authUrl = authUrl;
    }

    public String login(String username, String password) {
        RestClient authClient = RestClient.builder()
            .baseUrl(authUrl)
            .requestFactory(new SimpleClientHttpRequestFactory())
            .build();

        AuthResponse response = authClient.post()
            .uri("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(new LoginRequest(username, password))
            .retrieve()
            .body(AuthResponse.class);
        if (response == null || response.token() == null) {
            throw new IllegalStateException("No token returned for " + username);
        }
        return response.token();
    }

    private record LoginRequest(String username, String password) {
    }

    private record AuthResponse(String token) {
    }
}
