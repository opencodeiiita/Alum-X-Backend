package com.opencode.alumxbackend.auth;

import com.opencode.alumxbackend.auth.dto.CreateAdminRequest;
import com.opencode.alumxbackend.auth.dto.LoginRequest;
import com.opencode.alumxbackend.auth.dto.LoginResponse;
import com.opencode.alumxbackend.auth.dto.RegisterRequest;
import com.opencode.alumxbackend.auth.dto.RegisterResponse;
import com.opencode.alumxbackend.users.dto.UserResponseDto;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.model.UserRole;
import com.opencode.alumxbackend.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AdminFunctionalityTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.creation.secret}")
    private String adminCreationSecret;

    private WebClient webClient;
    private String adminToken;
    private String studentToken;

    @BeforeEach
    void setup() {
        webClient = WebClient.create("http://localhost:" + port);
        userRepository.deleteAll();

        // Create an admin user for testing
        User admin = User.builder()
                .username("adminuser")
                .email("admin@test.com")
                .name("Admin User")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.ADMIN)
                .profileCompleted(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRepository.save(admin);

        // Create a student user for testing
        User student = User.builder()
                .username("studentuser")
                .email("student@test.com")
                .name("Student User")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.STUDENT)
                .profileCompleted(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRepository.save(student);

        // Login as admin
        LoginRequest adminLogin = new LoginRequest("adminuser", "password123");
        LoginResponse adminLoginResponse = webClient.post()
                .uri("/api/auth/login")
                .bodyValue(adminLogin)
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .block();
        adminToken = adminLoginResponse.getAccessToken();

        // Login as student
        LoginRequest studentLogin = new LoginRequest("studentuser", "password123");
        LoginResponse studentLoginResponse = webClient.post()
                .uri("/api/auth/login")
                .bodyValue(studentLogin)
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .block();
        studentToken = studentLoginResponse.getAccessToken();
    }

    @Test
    @DisplayName("Should block ADMIN role from self-registration")
    void testBlockAdminSelfRegistration() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("hacker")
                .name("Hacker User")
                .email("hacker@test.com")
                .password("password123")
                .role("ADMIN")
                .build();

        WebClientResponseException exception = assertThrows(
                WebClientResponseException.class,
                () -> webClient.post()
                        .uri("/api/auth/register")
                        .bodyValue(registerRequest)
                        .retrieve()
                        .bodyToMono(RegisterResponse.class)
                        .block()
        );

        assertThat(exception.getStatusCode().value()).isEqualTo(400);
        // Check response body contains error message
        String responseBody = exception.getResponseBodyAsString();
        assertThat(responseBody).contains("Cannot register as ADMIN");
    }

    @Test
    @DisplayName("Should create admin with valid secret")
    void testCreateAdminWithValidSecret() {
        CreateAdminRequest createAdminRequest = CreateAdminRequest.builder()
                .username("newadmin")
                .name("New Admin")
                .email("newadmin@test.com")
                .password("password123")
                .build();

        RegisterResponse response = webClient.post()
                .uri("/api/auth/create-admin")
                .header("X-ADMIN-SECRET", adminCreationSecret)
                .bodyValue(createAdminRequest)
                .retrieve()
                .bodyToMono(RegisterResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("newadmin");
        assertThat(response.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(response.getMessage()).isEqualTo("Admin created successfully");

        // Verify admin was saved with ADMIN role
        User savedAdmin = userRepository.findByUsername("newadmin").orElse(null);
        assertThat(savedAdmin).isNotNull();
        assertThat(savedAdmin.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("Should reject admin creation with invalid secret")
    void testCreateAdminWithInvalidSecret() {
        CreateAdminRequest createAdminRequest = CreateAdminRequest.builder()
                .username("fakeadmin")
                .name("Fake Admin")
                .email("fakeadmin@test.com")
                .password("password123")
                .build();

        WebClientResponseException exception = assertThrows(
                WebClientResponseException.class,
                () -> webClient.post()
                        .uri("/api/auth/create-admin")
                        .header("X-ADMIN-SECRET", "wrongsecret")
                        .bodyValue(createAdminRequest)
                        .retrieve()
                        .bodyToMono(RegisterResponse.class)
                        .block()
        );

        assertThat(exception.getStatusCode().value()).isEqualTo(400);
        // Check response body contains error message
        String responseBody = exception.getResponseBodyAsString();
        assertThat(responseBody).contains("Invalid admin creation secret");
    }

    @Test
    @DisplayName("Should reject admin creation without secret header")
    void testCreateAdminWithoutSecret() {
        CreateAdminRequest createAdminRequest = CreateAdminRequest.builder()
                .username("nosecretadmin")
                .name("No Secret Admin")
                .email("nosecret@test.com")
                .password("password123")
                .build();

        WebClientResponseException exception = assertThrows(
                WebClientResponseException.class,
                () -> webClient.post()
                        .uri("/api/auth/create-admin")
                        .bodyValue(createAdminRequest)
                        .retrieve()
                        .bodyToMono(RegisterResponse.class)
                        .block()
        );

        // Should get 400 or 401 because header is missing
        assertThat(exception.getStatusCode().value()).isIn(400, 401);
    }

    @Test
    @DisplayName("Admin should be able to access GET /api/users")
    void testAdminCanAccessGetAllUsers() {
        List<UserResponseDto> users = webClient.get()
                .uri("/api/users")
                .header("Authorization", "Bearer " + adminToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<UserResponseDto>>() {})
                .block();

        assertThat(users).isNotNull();
        assertThat(users.size()).isGreaterThanOrEqualTo(2); // At least admin and student
    }

    @Test
    @DisplayName("Non-admin should get 403 on GET /api/users")
    void testNonAdminCannotAccessGetAllUsers() {
        WebClientResponseException exception = assertThrows(
                WebClientResponseException.class,
                () -> webClient.get()
                        .uri("/api/users")
                        .header("Authorization", "Bearer " + studentToken)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<UserResponseDto>>() {})
                        .block()
        );

        assertThat(exception.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    @DisplayName("Unauthenticated user should get 401 on GET /api/users")
    void testUnauthenticatedUserCannotAccessGetAllUsers() {
        WebClientResponseException exception = assertThrows(
                WebClientResponseException.class,
                () -> webClient.get()
                        .uri("/api/users")
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<UserResponseDto>>() {})
                        .block()
        );

        assertThat(exception.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    @DisplayName("Admin can login and get ROLE_ADMIN authority")
    void testAdminLoginGetsCorrectRole() {
        LoginRequest loginRequest = new LoginRequest("adminuser", "password123");
        LoginResponse response = webClient.post()
                .uri("/api/auth/login")
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getUser().getRole()).isEqualTo("ADMIN");
        assertThat(response.getAccessToken()).isNotNull();
    }
}
