package com.thex.chat.auth.controller;

import com.thex.chat.auth.dto.AuthResponse;
import com.thex.chat.auth.dto.LoginRequest;
import com.thex.chat.auth.dto.SignUpRequest;
import com.thex.chat.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse signUp(@Valid @RequestBody SignUpRequest request) {
        return authService.signUp(request);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
