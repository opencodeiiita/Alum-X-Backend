package com.opencode.alumxbackend.users.controller;

import com.opencode.alumxbackend.users.dto.UserRequest;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.model.UserRole;
import com.opencode.alumxbackend.users.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dev/users")
@RequiredArgsConstructor
public class UserController {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private static final String DUMMY_TOKEN = "alumx-dev-token";

        private static final java.util.logging.Logger logger = java.util.logging.Logger
                        .getLogger(UserController.class.getName());

        /**
         * Creates a new user in the database.
         * This endpoint is restricted to development/testing environments and requires
         * a valid X-DUMMY-TOKEN header.
         *
         * @param token   The mandatory security token passed in the X-DUMMY-TOKEN
         *                header.
         * @param request The request body containing user details (username, email,
         *                role, etc.).
         * @return ResponseEntity containing the creation status and basic user info.
         */
        @PostMapping
        public ResponseEntity<?> createUser(@RequestHeader(value = "X-DUMMY-TOKEN", required = false) String token,
                        @Valid @RequestBody UserRequest request) {

                // 1. Validate Token
                if (token == null || !token.equals(DUMMY_TOKEN)) {
                        logger.warning("Unathorized access attempt to Dev API. Missing or invalid token.");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(Map.of("error", "Invalid or missing X-DUMMY-TOKEN header"));
                }

                // 2. Validate Uniqueness
                if (userRepository.existsByEmail(request.getEmail())) {
                        logger.info("Attempt to create duplicate user with email: " + request.getEmail());
                        return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
                }
                if (userRepository.existsByUsername(request.getUsername())) {
                        logger.info("Attempt to create duplicate user with username: " + request.getUsername());
                        return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
                }

                // 3. Create User
                logger.info("Creating new user: " + request.getUsername() + " with role: " + request.getRole());

                try {
                        User user = User.builder()
                                        .username(request.getUsername())
                                        .name(request.getName())
                                        .email(request.getEmail())
                                        .passwordHash(passwordEncoder.encode(request.getPassword()))
                                        .role(UserRole.valueOf(request.getRole().toUpperCase()))
                                        .profileCompleted(true) // Set default reasonable for dev user
                                        .build();

                        userRepository.save(user);
                        logger.info("User created successfully: " + user.getId());

                        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                                        "message", "User created successfully via DEV API",
                                        "userId", user.getId(),
                                        "role", user.getRole().name()));
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest()
                                        .body(Map.of("error", "Invalid Role. Must be STUDENT, ALUMNI, or PROFESSOR."));
                }


        }
}
