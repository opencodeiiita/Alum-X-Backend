package com.opencode.alumxbackend.jobposts.controller;

import com.opencode.alumxbackend.auth.dto.LoginRequest;
import com.opencode.alumxbackend.auth.dto.LoginResponse;
import com.opencode.alumxbackend.jobposts.dto.PagedPostResponse;
import com.opencode.alumxbackend.jobposts.model.JobPost;
import com.opencode.alumxbackend.jobposts.repository.JobPostRepository;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.model.UserRole;
import com.opencode.alumxbackend.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Post Search & Filtering functionality.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PostSearchIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JobPostRepository jobPostRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private WebClient webClient;
    private String accessToken;

    @BeforeEach
    void setUp() {
        webClient = WebClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        // Clean up
        jobPostRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = User.builder()
                .username("searchtestuser")
                .email("searchtest@test.com")
                .name("Search Test User")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.STUDENT)
                .profileCompleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser = userRepository.save(testUser);

        // Login to get access token
        LoginRequest loginRequest = new LoginRequest("searchtest@test.com", "password123");
        LoginResponse loginResponse = webClient.post()
                .uri("/api/auth/login")
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .block();
        accessToken = loginResponse.getAccessToken();

        // Create test posts with different descriptions and dates
        createTestPost("Java Backend Developer position available", 
                LocalDateTime.now().minusDays(10));
        createTestPost("Python Data Science role with machine learning", 
                LocalDateTime.now().minusDays(5));
        createTestPost("Frontend React Developer needed for startup", 
                LocalDateTime.now().minusDays(3));
        createTestPost("Java Full Stack Engineer with Spring Boot experience", 
                LocalDateTime.now().minusDays(1));
        createTestPost("DevOps Engineer with Kubernetes and Docker knowledge", 
                LocalDateTime.now());
    }

    private void createTestPost(String description, LocalDateTime createdAt) {
        JobPost post = JobPost.builder()
                .username(testUser.getUsername())
                .description(description)
                .createdAt(createdAt)
                .build();
        jobPostRepository.save(post);
    }

    @Test
    void testSearchByKeyword_CaseInsensitive() {
        PagedPostResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/posts/search")
                        .queryParam("keyword", "java")
                        .build())                .header("Authorization", "Bearer " + accessToken)                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(PagedPostResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getPosts()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getPosts().get(0).getContent().toLowerCase()).contains("java");
        assertThat(response.getPosts().get(1).getContent().toLowerCase()).contains("java");
    }

    @Test
    void testSearchByKeyword_UpperCase() {
        PagedPostResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/posts/search")
                        .queryParam("keyword", "PYTHON")
                        .build())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(PagedPostResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getPosts()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getPosts().get(0).getContent().toLowerCase()).contains("python");
    }

    @Test
    void testSearchByKeyword_PartialMatch() {
        PagedPostResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/posts/search")
                        .queryParam("keyword", "dev")
                        .build())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(PagedPostResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getPosts().size()).isGreaterThanOrEqualTo(3);
        assertThat(response.getTotalElements()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void testSearchByDateRange() {
        LocalDateTime from = LocalDateTime.now().minusDays(4);
        LocalDateTime to = LocalDateTime.now().plusDays(1);

        PagedPostResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/posts/search")
                        .queryParam("dateFrom", from.toString())
                        .queryParam("dateTo", to.toString())
                        .build())                .header("Authorization", "Bearer " + accessToken)                .retrieve()
                .bodyToMono(PagedPostResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getPosts()).hasSize(3);
        assertThat(response.getTotalElements()).isEqualTo(3);
    }

    @Test
    void testSearchByDateFrom() {
        LocalDateTime from = LocalDateTime.now().minusDays(2);

        PagedPostResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/posts/search")
                        .queryParam("dateFrom", from.toString())
                        .build())                .header("Authorization", "Bearer " + accessToken)                .retrieve()
                .bodyToMono(PagedPostResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getPosts()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
    }

    @Test
    void testSearchByDateTo() {
        LocalDateTime to = LocalDateTime.now().minusDays(4);

        PagedPostResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/posts/search")
                        .queryParam("dateTo", to.toString())
                        .build())                .header("Authorization", "Bearer " + accessToken)                .retrieve()
                .bodyToMono(PagedPostResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getPosts()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
    }

    @Test
    void testSearchWithKeywordAndDateRange() {
        LocalDateTime from = LocalDateTime.now().minusDays(6);
        LocalDateTime to = LocalDateTime.now();

        PagedPostResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/posts/search")
                        .queryParam("keyword", "developer")
                        .queryParam("dateFrom", from.toString())
                        .queryParam("dateTo", to.toString())
                        .build())                .header("Authorization", "Bearer " + accessToken)                .retrieve()
                .bodyToMono(PagedPostResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getPosts().size()).isGreaterThanOrEqualTo(1);
        assertThat(response.getTotalElements()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void testPagination_FirstPage() {
        PagedPostResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/posts/search")
                        .queryParam("page", "0")
                        .queryParam("size", "2")
                        .build())                .header("Authorization", "Bearer " + accessToken)                .retrieve()
                .bodyToMono(PagedPostResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getPosts()).hasSize(2);
        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getPageSize()).isEqualTo(2);
        assertThat(response.getTotalPages()).isEqualTo(3);
        assertThat(response.getTotalElements()).isEqualTo(5);
        assertThat(response.getIsFirst()).isTrue();
        assertThat(response.getIsLast()).isFalse();
        assertThat(response.getHasNext()).isTrue();
        assertThat(response.getHasPrevious()).isFalse();
    }

    @Test
    void testPagination_SecondPage() {
        PagedPostResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/posts/search")
                        .queryParam("page", "1")
                        .queryParam("size", "2")
                        .build())                .header("Authorization", "Bearer " + accessToken)                .retrieve()
                .bodyToMono(PagedPostResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getPosts()).hasSize(2);
        assertThat(response.getCurrentPage()).isEqualTo(1);
        assertThat(response.getPageSize()).isEqualTo(2);
        assertThat(response.getIsFirst()).isFalse();
        assertThat(response.getHasNext()).isTrue();
        assertThat(response.getHasPrevious()).isTrue();
    }

    @Test
    void testPagination_LastPage() {
        PagedPostResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/posts/search")
                        .queryParam("page", "2")
                        .queryParam("size", "2")
                        .build())                .header("Authorization", "Bearer " + accessToken)                .retrieve()
                .bodyToMono(PagedPostResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getPosts()).hasSize(1);
        assertThat(response.getCurrentPage()).isEqualTo(2);
        assertThat(response.getIsLast()).isTrue();
        assertThat(response.getHasNext()).isFalse();
        assertThat(response.getHasPrevious()).isTrue();
    }

    @Test
    void testDefaultPagination() {
        PagedPostResponse response = webClient.get()
                .uri("/api/posts/search")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(PagedPostResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getPosts()).hasSize(5);
        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getPageSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(5);
    }

    @Test
    void testSearchNoResults() {
        PagedPostResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/posts/search")
                        .queryParam("keyword", "nonexistentkeyword12345")
                        .build())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(PagedPostResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getPosts()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getTotalPages()).isEqualTo(0);
    }

    @Test
    void testSearchAllPosts_NoFilters() {
        PagedPostResponse response = webClient.get()
                .uri("/api/posts/search")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(PagedPostResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getPosts()).hasSize(5);
        assertThat(response.getTotalElements()).isEqualTo(5);
    }

    @Test
    void testPaginationSizeLimit() {
        // Test that size is capped at max (100)
        PagedPostResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/posts/search")
                        .queryParam("page", "0")
                        .queryParam("size", "200")
                        .build())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(PagedPostResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getPageSize()).isEqualTo(100);
    }

    @Test
    void testNegativePageNumber() {
        // Test that negative page numbers default to 0
        PagedPostResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/posts/search")
                        .queryParam("page", "-1")
                        .queryParam("size", "10")
                        .build())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(PagedPostResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getCurrentPage()).isEqualTo(0);
    }

    @Test
    void testResultsOrderedByCreatedAtDesc() {
        PagedPostResponse response = webClient.get()
                .uri("/api/posts/search")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(PagedPostResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getPosts()).hasSize(5);
        // Most recent post should be first
        assertThat(response.getPosts().get(0).getContent().toLowerCase()).contains("devops");
        // Oldest post should be last
        assertThat(response.getPosts().get(4).getContent().toLowerCase()).contains("backend");
    }
}
