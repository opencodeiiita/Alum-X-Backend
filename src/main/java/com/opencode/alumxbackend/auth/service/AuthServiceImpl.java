package com.opencode.alumxbackend.auth.service;

import com.opencode.alumxbackend.auth.dto.CreateAdminRequest;
import com.opencode.alumxbackend.auth.dto.LoginRequest;
import com.opencode.alumxbackend.auth.dto.LoginResponse;
import com.opencode.alumxbackend.auth.dto.RegisterRequest;
import com.opencode.alumxbackend.auth.dto.RegisterResponse;
import com.opencode.alumxbackend.auth.exception.InvalidCredentialsException;
import com.opencode.alumxbackend.auth.security.JwtTokenProvider;
import com.opencode.alumxbackend.common.exception.Errors.BadRequestException;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.model.UserRole;
import com.opencode.alumxbackend.users.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // Find user by email or username
        User user = userRepository.findByEmail(loginRequest.getEmailOrUsername())
                .orElseGet(() -> userRepository.findByUsername(loginRequest.getEmailOrUsername())
                        .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials")));

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        // Generate token
        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name()
        );

        // Build response
        LoginResponse.UserBasicInfo userInfo = new LoginResponse.UserBasicInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getRole().name()
        );

        LoginResponse response = new LoginResponse();
        response.setAccessToken(token);
        response.setTokenExpiryTime(jwtTokenProvider.getExpirationTime());
        response.setUser(userInfo);

        return response;
    }

    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email already exists: " + registerRequest.getEmail());
        }

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Username already exists: " + registerRequest.getUsername());
        }

        UserRole role;
        try {
            role = UserRole.valueOf(registerRequest.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role. Must be STUDENT, ALUMNI, or PROFESSOR.");
        }

        // Block ADMIN role from self-registration
        if (role == UserRole.ADMIN) {
            throw new BadRequestException("Cannot register as ADMIN. ADMIN accounts can only be created through authorized channels.");
        }

        if (!registerRequest.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}(\\.[A-Za-z]{2,})?$")) {
            throw new BadRequestException("Invalid email format: " + registerRequest.getEmail());
        }

        if (registerRequest.getPassword().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .role(role)
                .profileCompleted(true) // default for dev
                .build();

        User savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .createdAt(savedUser.getCreatedAt())
                .message("User registered successfully")
                .build();
    }

    @Override
    public RegisterResponse createAdmin(CreateAdminRequest createAdminRequest) {
        // Check if email already exists
        if (userRepository.existsByEmail(createAdminRequest.getEmail())) {
            throw new BadRequestException("Email already exists: " + createAdminRequest.getEmail());
        }

        // Check if username already exists
        if (userRepository.existsByUsername(createAdminRequest.getUsername())) {
            throw new BadRequestException("Username already exists: " + createAdminRequest.getUsername());
        }

        // Validate email format
        if (!createAdminRequest.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}(\\.[A-Za-z]{2,})?$")) {
            throw new BadRequestException("Invalid email format: " + createAdminRequest.getEmail());
        }

        // Validate password length
        if (createAdminRequest.getPassword().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }

        // Create admin user with ADMIN role
        User admin = User.builder()
                .username(createAdminRequest.getUsername())
                .name(createAdminRequest.getName())
                .email(createAdminRequest.getEmail())
                .passwordHash(passwordEncoder.encode(createAdminRequest.getPassword()))
                .role(UserRole.ADMIN) // Set role to ADMIN server-side
                .profileCompleted(true)
                .build();

        User savedAdmin = userRepository.save(admin);

        return RegisterResponse.builder()
                .userId(savedAdmin.getId())
                .username(savedAdmin.getUsername())
                .email(savedAdmin.getEmail())
                .role(savedAdmin.getRole())
                .createdAt(savedAdmin.getCreatedAt())
                .message("Admin created successfully")
                .build();
    }
}
