package com.opencode.alumxbackend.auth.controller;

import com.opencode.alumxbackend.auth.dto.CreateAdminRequest;
import com.opencode.alumxbackend.auth.dto.LoginRequest;
import com.opencode.alumxbackend.auth.dto.LoginResponse;
import com.opencode.alumxbackend.auth.dto.RegisterRequest;
import com.opencode.alumxbackend.auth.dto.RegisterResponse;
import com.opencode.alumxbackend.auth.service.AuthService;
import com.opencode.alumxbackend.common.exception.Errors.BadRequestException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    
    @Value("${admin.creation.secret}")
    private String adminCreationSecret;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        RegisterResponse response = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/create-admin")
    public ResponseEntity<RegisterResponse> createAdmin(
            @RequestHeader("X-ADMIN-SECRET") String providedSecret,
            @Valid @RequestBody CreateAdminRequest createAdminRequest) {
        
        // Validate the secret header
        if (!adminCreationSecret.equals(providedSecret)) {
            throw new BadRequestException("Invalid admin creation secret");
        }
        
        RegisterResponse response = authService.createAdmin(createAdminRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
