package com.thex.chat.auth.service;

import com.thex.chat.auth.dto.AuthResponse;
import com.thex.chat.auth.dto.LoginRequest;
import com.thex.chat.auth.dto.SignUpRequest;
import com.thex.chat.auth.model.Role;
import com.thex.chat.auth.model.User;
import com.thex.chat.auth.repository.UserRepository;
import com.thex.chat.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse signUp(SignUpRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = User.builder()
            .username(request.username())
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .roles(Set.of(Role.USER))
            .build();
        userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        String token = jwtTokenProvider.generateToken(authentication);
        return new AuthResponse(token, user.getUsername());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        String token = jwtTokenProvider.generateToken(authentication);
        return new AuthResponse(token, request.username());
    }
}
