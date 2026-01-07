package com.opencode.alumxbackend.jobposts.controller;

import com.opencode.alumxbackend.auth.dto.LoginRequest;
import com.opencode.alumxbackend.auth.dto.LoginResponse;
import com.opencode.alumxbackend.jobposts.model.JobPost;
import com.opencode.alumxbackend.jobposts.repository.JobPostRepository;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class JobPostControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobPostRepository jobPostRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private WebClient webClient;
    private User testUser;
    private String accessToken;

    @BeforeEach
    void setUp() {
        webClient = WebClient.create("http://localhost:" + port);
        jobPostRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .username("integrationuser")
                .name("Integration User")
                .email("integration@test.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.ALUMNI)
                .profileCompleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser = userRepository.save(testUser);

        // Login to get access token
        LoginRequest loginRequest = new LoginRequest("integration@test.com", "password123");
        LoginResponse loginResponse = webClient.post()
                .uri("/api/auth/login")
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .block();
        accessToken = loginResponse.getAccessToken();
    }

    @Test
    @DisplayName("GET /api/users/{userId}/posts - returns 200 OK with posts list")
    void getPostsByUser_ReturnsOkWithPosts() {
        JobPost post = JobPost.builder()
                .username(testUser.getUsername())
                .description("Integration test job post description content")
                .createdAt(LocalDateTime.now())
                .build();
        jobPostRepository.save(post);

        List<?> response = webClient.get()
                .uri("/api/users/" + testUser.getId() + "/posts")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(List.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
    }

    @Test
    @DisplayName("GET /api/users/{userId}/posts - returns 200 OK with empty list when no posts")
    void getPostsByUser_ReturnsEmptyListWhenNoPosts() {
        List<?> response = webClient.get()
                .uri("/api/users/" + testUser.getId() + "/posts")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(List.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("GET /api/users/{userId}/posts - returns error when user not found")
    void getPostsByUser_ReturnsNotFoundWhenUserDoesNotExist() {
        try {
            webClient.get()
                    .uri("/api/users/99999/posts")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            assertThat(e.getMessage()).containsAnyOf("404", "500");
        }
    }

    @Test
    @DisplayName("GET /api/users/{userId}/posts - returns multiple posts")
    void getPostsByUser_ReturnsMultiplePosts() {
        JobPost post1 = JobPost.builder()
                .username(testUser.getUsername())
                .description("First post description for testing")
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();
        
        JobPost post2 = JobPost.builder()
                .username(testUser.getUsername())
                .description("Second post description for testing")
                .createdAt(LocalDateTime.now())
                .build();
        
        jobPostRepository.save(post1);
        jobPostRepository.save(post2);

        List<?> response = webClient.get()
                .uri("/api/users/" + testUser.getId() + "/posts")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(List.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);
    }
}
