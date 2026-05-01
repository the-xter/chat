package com.thex.chat.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class UserSecurity {

    public boolean isOwner(Authentication authentication, Integer userId) {
        if (authentication == null || userId == null) {
            return false;
        }
        return authentication.getPrincipal() instanceof AuthenticatedUser principal
            && userId.equals(principal.getId());
    }
}
