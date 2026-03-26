package com.thex.chat.auth.dto;

import com.thex.chat.auth.model.Role;

import java.util.Set;

public record AuthResponse(
    String token,
    String username,
    Set<Role> roles
) {
}
