package com.opencode.alumxbackend.users.controller;

import com.opencode.alumxbackend.users.dto.UserProfileDTO;
import com.opencode.alumxbackend.users.dto.UserRequest;
import com.opencode.alumxbackend.users.model.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.logging.Logger;

import com.opencode.alumxbackend.users.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService; // use service instead of repository
//     private static final String DUMMY_TOKEN = "alumx-dev-token";
    private static final Logger logger = Logger.getLogger(UserController.class.getName());

    @PostMapping
    public ResponseEntity<?> createUser(
            @RequestHeader(value = "X-DUMMY-TOKEN", required = false) String token,
            @Valid @RequestBody UserRequest request) {

//        if (token == null || !token.equals(DUMMY_TOKEN)) {
//            logger.warning("Unauthorized access attempt to Dev API. Missing or invalid token.");
//            throw new UnauthorizedAccessException("Invalid or missing X-DUMMY-TOKEN header");
//        }

        logger.info("Creating new user: " + request.getUsername() + " with role: " + request.getRole());
        User user = userService.createUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "User created successfully via DEV API",
                "userId", user.getId(),
                "role", user.getRole().name()
        ));
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable Long userId){
        return userService.getUserProfile(userId)
                .map(profile -> ResponseEntity.ok(profile))
                .orElseGet(()->ResponseEntity.notFound().build());
    }
}
