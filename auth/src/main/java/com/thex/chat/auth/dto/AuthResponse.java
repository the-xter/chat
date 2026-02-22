package com.thex.chat.auth.dto;

public record AuthResponse(
    String token,
    String username
) {
}
