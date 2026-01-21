package com.opencode.alumxbackend.auth;

import com.opencode.alumxbackend.auth.dto.LoginRequest;
import com.opencode.alumxbackend.auth.dto.LoginResponse;
import com.opencode.alumxbackend.jobposts.repository.CommentRepository;
import com.opencode.alumxbackend.notifications.repository.NotificationRepository;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.model.UserRole;
import com.opencode.alumxbackend.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private WebClient webClient;
    private User testUser;



    // used for setup before every test method
    @BeforeEach
    void setUp() {
        webClient = WebClient.create("http://localhost:" + port);

        // Clean up dependent entities first to avoid foreign key constraint violations
        commentRepository.deleteAll();
        notificationRepository.deleteAll();
        userRepository.deleteAll(); // this line is used to clean teh database

        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .name("Test User")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.STUDENT)
                .profileCompleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser = userRepository.save(testUser);
    }



    @Test
    @DisplayName("Login with valid credentials should return success")
    void loginWithValidCredentials_shouldReturnSuccess() {
        LoginRequest loginRequest = new LoginRequest(
                "test@example.com",
                "password123");

        // we created a new LoginRest( of our exising user )

        LoginResponse response = webClient.post()
                .uri("/api/auth/login")
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .block();



        System.out.println("========== LOGIN RESPONSE ==========");
        System.out.println("Access Token     : " + response.getAccessToken());
        System.out.println("Token Expiry Time: " + response.getTokenExpiryTime());
        System.out.println("User ID          : " + response.getUser().getId());
        System.out.println("User Email       : " + response.getUser().getEmail());
        System.out.println("Username         : " + response.getUser().getUsername());
        System.out.println("====================================");

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getAccessToken()).isNotEmpty();
        assertThat(response.getTokenExpiryTime()).isNotNull();
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Login with invalid credentials should return error")
    void loginWithInvalidCredentials_shouldReturnError() {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "wrongpassword");

        WebClientResponseException exception = assertThrows(WebClientResponseException.class, () -> {
            webClient.post()
                    .uri("/api/auth/login")
                    .bodyValue(loginRequest)
                    .retrieve()
                    .bodyToMono(LoginResponse.class)
                    .block();
        });

        // Invalid credentials returns 401 Unauthorized
        assertThat(exception.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    @DisplayName("Login with non-existent user should return error")
    void loginWithNonExistentUser_shouldReturnError() {
        LoginRequest loginRequest = new LoginRequest("nonexistent@example.com", "password123");

        WebClientResponseException exception = assertThrows(WebClientResponseException.class, () -> {
            webClient.post()
                    .uri("/api/auth/login")
                    .bodyValue(loginRequest)
                    .retrieve()
                    .bodyToMono(LoginResponse.class)
                    .block();
        });

        // Non-existent user returns 401 Unauthorized
        assertThat(exception.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    @DisplayName("Access protected API with valid token should return success")
    void accessProtectedApiWithValidToken_shouldReturnSuccess() {
        // First login to get token
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");
        LoginResponse loginResponse = webClient.post()
                .uri("/api/auth/login")
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .block();

        assertThat(loginResponse).isNotNull();
        String accessToken = loginResponse.getAccessToken();

        // Access protected endpoint with token
        String response = webClient.get()
                .uri("/api/users/" + testUser.getId() + "/profile")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Access protected API without token should return error")
    void accessProtectedApiWithoutToken_shouldReturnError() {
        WebClientResponseException exception = assertThrows(WebClientResponseException.class, () -> {
            webClient.get()
                    .uri("/api/users/" + testUser.getId() + "/profile")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        });

        // Without token returns 401 Unauthorized
        assertThat(exception.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    @DisplayName("Access protected API with invalid token should return error")
    void accessProtectedApiWithInvalidToken_shouldReturnError() {
        String invalidToken = "invalid.token.here";

        WebClientResponseException exception = assertThrows(WebClientResponseException.class, () -> {
            webClient.get()
                    .uri("/api/users/" + testUser.getId() + "/profile")
                    .header("Authorization", "Bearer " + invalidToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        });

        // Invalid token returns 401 Unauthorized
        assertThat(exception.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    @DisplayName("Login with username instead of email should work")
    void loginWithUsername_shouldReturnSuccess() {
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");

        LoginResponse response = webClient.post()
                .uri("/api/auth/login")
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getAccessToken()).isNotEmpty();
    }
}
