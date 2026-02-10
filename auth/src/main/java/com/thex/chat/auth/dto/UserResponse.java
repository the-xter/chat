package com.thex.chat.auth.dto;

import com.thex.chat.auth.model.Role;
import com.thex.chat.auth.model.User;

import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        String email,
        Set<Role> roles
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRoles());
    }
}
