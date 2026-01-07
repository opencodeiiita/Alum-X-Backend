package com.opencode.alumxbackend.search.controller;

import com.opencode.alumxbackend.auth.dto.LoginRequest;
import com.opencode.alumxbackend.auth.dto.LoginResponse;
import com.opencode.alumxbackend.notifications.repository.NotificationRepository;
import com.opencode.alumxbackend.users.dto.UserResponseDto;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.model.UserRole;
import com.opencode.alumxbackend.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserSearchControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private WebClient webClient;
    private String authToken;

    @BeforeEach
    void setup() {
        webClient = WebClient.create("http://localhost:" + port);
        // Clean up in proper order to avoid foreign key constraints
        notificationRepository.deleteAll();
        userRepository.deleteAll();
        createUser("johnDoe", "John Doe", "john@test.com");
        createUser("janeSmith", "Jane Smith", "jane@test.com");
        createUser("alice123", "Alice Wonder", "alice@test.com");
        createUser("bob_builder", "Bob Builder", "bob@test.com");
        createUser("charlie99", "Charlie Brown", "charlie@test.com");
        LoginRequest loginRequest = new LoginRequest("johnDoe", "password123");
        LoginResponse loginResponse = webClient.post()
                .uri("/api/auth/login")
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .block();
        authToken = loginResponse.getAccessToken();
    }

    private void createUser(String username, String name, String email) {
        User user = User.builder()
                .username(username)
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.STUDENT)
                .profileCompleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRepository.save(user);
    }

    private List<UserResponseDto> searchUsers(String query) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/users/search").queryParam("q", query).build())
                .header("Authorization", "Bearer " + authToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<UserResponseDto>>() {})
                .block();
    }

    private HttpStatusCode searchUsersAndGetStatus(String query) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/users/search").queryParam("q", query).build())
                .header("Authorization", "Bearer " + authToken)
                .exchangeToMono(response -> Mono.just(response.statusCode()))
                .block();
    }

    @Nested
    @DisplayName("Positive Test Cases - Valid Search Queries")
    class PositiveTests {
        @Test
        @DisplayName("Should return users when searching by partial username")
        void searchByPartialUsername() {
            List<UserResponseDto> result = searchUsers("john");
            assertThat(result).isNotEmpty();
            assertThat(result.get(0).getName()).contains("John");
        }

        @Test
        @DisplayName("Should return users when searching by full username")
        void searchByFullUsername() {
            List<UserResponseDto> result = searchUsers("johnDoe");
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Should return users when searching by name")
        void searchByName() {
            List<UserResponseDto> result = searchUsers("Jane");
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Should return multiple users for common search term")
        void searchReturnsMultipleUsers() {
            List<UserResponseDto> result = searchUsers("j");
            assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Should return users when searching with numbers in query")
        void searchWithNumbers() {
            List<UserResponseDto> result = searchUsers("alice123");
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Should return users when searching with underscore in query")
        void searchWithUnderscore() {
            List<UserResponseDto> result = searchUsers("bob_");
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Should handle case-insensitive search")
        void searchCaseInsensitive() {
            List<UserResponseDto> result = searchUsers("JOHN");
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Should trim whitespace from search query")
        void searchWithWhitespace() {
            List<UserResponseDto> result = searchUsers("  john  ");
            assertThat(result).isNotEmpty();
        }
    }


    @Nested
    @DisplayName("Negative Test Cases - Invalid Search Queries")
    class NegativeTests {
        @Test
        @DisplayName("Should return bad request when query is empty")
        void searchWithEmptyQuery() {
            HttpStatusCode status = searchUsersAndGetStatus("");
            assertThat(status.is4xxClientError() || status.is5xxServerError()).isTrue();
        }

        @Test
        @DisplayName("Should return bad request when query is only whitespace")
        void searchWithOnlyWhitespace() {
            HttpStatusCode status = searchUsersAndGetStatus("   ");
            assertThat(status.is4xxClientError() || status.is5xxServerError()).isTrue();
        }

        @Test
        @DisplayName("Should return empty list when no users match the query")
        void searchWithNoMatchingUsers() {
            List<UserResponseDto> result = searchUsers("nonexistentuser12345");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Symbol Test Cases - Queries with Special Characters")
    class SymbolTests {
        @Test
        @DisplayName("Should handle search query starting with @ symbol")
        void searchStartingWithAtSymbol() {
            List<UserResponseDto> result = searchUsers("@john");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle search query starting with # symbol")
        void searchStartingWithHashSymbol() {
            List<UserResponseDto> result = searchUsers("#user");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle search query starting with $ symbol")
        void searchStartingWithDollarSymbol() {
            List<UserResponseDto> result = searchUsers("$test");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle search query starting with ! symbol")
        void searchStartingWithExclamationSymbol() {
            List<UserResponseDto> result = searchUsers("!admin");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle search query with hyphen")
        void searchWithHyphen() {
            List<UserResponseDto> result = searchUsers("john-doe");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle search query with period")
        void searchWithPeriod() {
            List<UserResponseDto> result = searchUsers("john.doe");
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Username Validation - Usernames should not start with symbols")
    class UsernameValidationTests {
        @Test
        @DisplayName("Should not find users with username starting with @ symbol")
        void usernameStartingWithAtSymbol() {
            List<UserResponseDto> result = searchUsers("@");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should not find users with username starting with # symbol")
        void usernameStartingWithHashSymbol() {
            List<UserResponseDto> result = searchUsers("#");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should not find users with username starting with $ symbol")
        void usernameStartingWithDollarSymbol() {
            List<UserResponseDto> result = searchUsers("$");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should not find users with username starting with ! symbol")
        void usernameStartingWithExclamationSymbol() {
            List<UserResponseDto> result = searchUsers("!");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should not find users with username starting with * symbol")
        void usernameStartingWithAsteriskSymbol() {
            List<UserResponseDto> result = searchUsers("*");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should not find users with username starting with - symbol")
        void usernameStartingWithHyphenSymbol() {
            List<UserResponseDto> result = searchUsers("-");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should not find users with username starting with . symbol")
        void usernameStartingWithPeriodSymbol() {
            List<UserResponseDto> result = searchUsers(".");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Edge Cases - Boundary and Special Scenarios")
    class EdgeCaseTests {
        @Test
        @DisplayName("Should handle single character search")
        void searchWithSingleCharacter() {
            List<UserResponseDto> result = searchUsers("j");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle very long search query")
        void searchWithVeryLongQuery() {
            String longQuery = "a".repeat(100);
            List<UserResponseDto> result = searchUsers(longQuery);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle search with mixed case")
        void searchWithMixedCase() {
            List<UserResponseDto> result = searchUsers("JoHnDoE");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle search with numbers only")
        void searchWithNumbersOnly() {
            List<UserResponseDto> result = searchUsers("123");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle search with SQL injection attempt")
        void searchWithSqlInjectionAttempt() {
            List<UserResponseDto> result = searchUsers("'; DROP TABLE users; --");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle search with HTML tags")
        void searchWithHtmlTags() {
            List<UserResponseDto> result = searchUsers("<script>alert('xss')</script>");
            assertThat(result).isEmpty();
        }
    }
}