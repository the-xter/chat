package com.thex.chat.auth.service;

import com.thex.chat.auth.dto.UpdateUserRequest;
import com.thex.chat.auth.dto.UserResponse;
import com.thex.chat.auth.model.User;
import com.thex.chat.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(UserResponse::from)
            .toList();
    }

    public UserResponse getUserById(Integer id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return UserResponse.from(user);
    }

    public UserResponse updateUser(Integer id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.username() != null) {
            if (userRepository.existsByUsername(request.username()) &&
                !user.getUsername().equals(request.username())) {
                throw new IllegalArgumentException("Username is already taken");
            }
            user.setUsername(request.username());
        }
        if (request.email() != null) {
            if (userRepository.existsByEmail(request.email()) &&
                !user.getEmail().equals(request.email())) {
                throw new IllegalArgumentException("Email is already in use");
            }
            user.setEmail(request.email());
        }
        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        userRepository.save(user);
        return UserResponse.from(user);
    }

    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteById(id);
    }
}
