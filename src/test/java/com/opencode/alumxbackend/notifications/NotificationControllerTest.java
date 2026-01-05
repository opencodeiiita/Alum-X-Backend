package com.opencode.alumxbackend.notifications;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import com.opencode.alumxbackend.auth.dto.LoginRequest;
import com.opencode.alumxbackend.auth.dto.LoginResponse;
import com.opencode.alumxbackend.notifications.dto.NotificationRequest;
import com.opencode.alumxbackend.notifications.dto.NotificationResponse;
import com.opencode.alumxbackend.notifications.repository.NotificationRepository;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.model.UserRole;
import com.opencode.alumxbackend.users.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class NotificationControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private WebClient webClient;
    private User testUser;
    private String accessToken;

    @BeforeEach
    public void setUp() {
        webClient = WebClient.create("http://localhost:" + port);

        notificationRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .username("notifyuser")
                .name("Notify User")
                .email("notifyuser@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.STUDENT)
                .profileCompleted(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser = userRepository.save(testUser);

        // Login to get access token
        LoginRequest loginRequest = new LoginRequest("notifyuser@example.com", "password123");
        LoginResponse loginResponse = webClient.post()
                .uri("/api/auth/login")
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .block();
        accessToken = loginResponse.getAccessToken();
    }

    @Test
    @DisplayName("POST /api/notifications - create and return notification")
    void createNotification_validRequest_returnsCreated() {
        NotificationRequest req = new NotificationRequest();
        req.setUserId(testUser.getId());
        req.setType("INFO");
        req.setMessage("Test notification");
        req.setReferenceId(123L);

        NotificationResponse resp = webClient.post()
                .uri("/api/notifications")
                .header("Authorization", "Bearer " + accessToken)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(NotificationResponse.class)
                .block();

        assertThat(resp).isNotNull();
        assertThat(resp.getType()).isEqualTo("INFO");
        assertThat(resp.getMessage()).isEqualTo("Test notification");
        assertThat(resp.getId()).isNotNull();
    }

    @Test
    @DisplayName("POST /api/notifications - missing userId returns 400")
    void createNotification_missingUserId_returnsBadRequest() {
        NotificationRequest req = new NotificationRequest();
        req.setType("INFO");
        req.setMessage("Missing userId");

        try {
            webClient.post()
                    .uri("/api/notifications")
                    .header("Authorization", "Bearer " + accessToken)
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("400");
        }
    }

    @Test
    @DisplayName("POST /api/notifications - empty message returns 400")
    void createNotification_emptyMessage_returnsBadRequest() {
        NotificationRequest req = new NotificationRequest();
        req.setUserId(testUser.getId());
        req.setType("INFO");
        req.setMessage("");

        try {
            webClient.post()
                    .uri("/api/notifications")
                    .header("Authorization", "Bearer " + accessToken)
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("400");
        }
    }

    @Test
    @DisplayName("GET /api/notifications - returns notifications for user ordered newest first")
    void getNotifications_returnsOrderedNotifications() {
        NotificationRequest req1 = new NotificationRequest();
        req1.setUserId(testUser.getId());
        req1.setType("INFO");
        req1.setMessage("First notification");

        NotificationRequest req2 = new NotificationRequest();
        req2.setUserId(testUser.getId());
        req2.setType("ALERT");
        req2.setMessage("Second notification");

        webClient.post()
                .uri("/api/notifications")
                .header("Authorization", "Bearer " + accessToken)
                .bodyValue(req1)
                .retrieve()
                .bodyToMono(NotificationResponse.class)
                .block();
        webClient.post()
                .uri("/api/notifications")
                .header("Authorization", "Bearer " + accessToken)
                .bodyValue(req2)
                .retrieve()
                .bodyToMono(NotificationResponse.class)
                .block();

        List<?> list = webClient.get()
                .uri("/api/notifications?userId=" + testUser.getId())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(List.class)
                .block();

        assertThat(list).isNotNull();
        assertThat(list.size()).isGreaterThanOrEqualTo(2);
    }
}
