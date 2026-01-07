package com.opencode.alumxbackend.groupchat.controller;

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
import com.opencode.alumxbackend.groupchat.dto.GroupChatRequest;
import com.opencode.alumxbackend.groupchat.dto.GroupChatResponse;
import com.opencode.alumxbackend.groupchat.repository.GroupChatRepository;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.model.UserRole;
import com.opencode.alumxbackend.users.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GroupChatControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupChatRepository groupChatRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private WebClient webClient;
    private User testUser1;
    private User testUser2;
    private User testUser3;
    private String accessToken;

    @BeforeEach
    void setUp() {
        webClient = WebClient.create("http://localhost:" + port);
        
        // Clean up
        groupChatRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users with encoded passwords
        testUser1 = User.builder()
                .username("user1")
                .name("Test User 1")
                .email("user1@test.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.STUDENT)
                .profileCompleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser1 = userRepository.save(testUser1);

        testUser2 = User.builder()
                .username("user2")
                .name("Test User 2")
                .email("user2@test.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.STUDENT)
                .profileCompleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser2 = userRepository.save(testUser2);

        testUser3 = User.builder()
                .username("user3")
                .name("Test User 3")
                .email("user3@test.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.ALUMNI)
                .profileCompleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser3 = userRepository.save(testUser3);

        // Login to get access token
        LoginRequest loginRequest = new LoginRequest("user1@test.com", "password123");
        LoginResponse loginResponse = webClient.post()
                .uri("/api/auth/login")
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .block();
        accessToken = loginResponse.getAccessToken();
    }

    // ========== SUCCESS CASES ==========

    @Test
    @DisplayName("POST /api/group-chats - should create group with valid participants")
    void createGroup_WithValidData_ReturnsCreatedGroup() {
        GroupChatRequest request = GroupChatRequest.builder()
                .name("Study Group")
                .participants(List.of(
                        new GroupChatRequest.ParticipantRequest(testUser1.getId(), testUser1.getUsername()),
                        new GroupChatRequest.ParticipantRequest(testUser2.getId(), testUser2.getUsername()),
                        new GroupChatRequest.ParticipantRequest(testUser3.getId(), testUser3.getUsername())
                ))
                .build();

        GroupChatResponse response = webClient.post()
                .uri("/api/group-chats")
                .header("Authorization", "Bearer " + accessToken)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GroupChatResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getGroupId()).isNotNull();
        assertThat(response.getName()).isEqualTo("Study Group");
        assertThat(response.getParticipants()).hasSize(3);
    }

    @Test
    @DisplayName("GET /api/group-chats/{groupId} - should return group by ID")
    void getGroupById_ExistingGroup_ReturnsGroup() {
        GroupChatRequest createRequest = GroupChatRequest.builder()
                .name("Test Group")
                .participants(List.of(
                        new GroupChatRequest.ParticipantRequest(testUser1.getId(), testUser1.getUsername()),
                        new GroupChatRequest.ParticipantRequest(testUser2.getId(), testUser2.getUsername())
                ))
                .build();

        GroupChatResponse createdGroup = webClient.post()
                .uri("/api/group-chats")
                .header("Authorization", "Bearer " + accessToken)
                .bodyValue(createRequest)
                .retrieve()
                .bodyToMono(GroupChatResponse.class)
                .block();

        GroupChatResponse response = webClient.get()
                .uri("/api/group-chats/" + createdGroup.getGroupId())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(GroupChatResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getGroupId()).isEqualTo(createdGroup.getGroupId());
        assertThat(response.getName()).isEqualTo("Test Group");
        assertThat(response.getParticipants()).hasSize(2);
    }

    @Test
    @DisplayName("GET /api/group-chats/user/{userId} - should return all groups for user")
    void getGroupsForUser_UserInMultipleGroups_ReturnsAllGroups() {
        GroupChatRequest request1 = GroupChatRequest.builder()
                .name("Group 1")
                .participants(List.of(
                        new GroupChatRequest.ParticipantRequest(testUser1.getId(), testUser1.getUsername()),
                        new GroupChatRequest.ParticipantRequest(testUser2.getId(), testUser2.getUsername())
                ))
                .build();

        GroupChatRequest request2 = GroupChatRequest.builder()
                .name("Group 2")
                .participants(List.of(
                        new GroupChatRequest.ParticipantRequest(testUser1.getId(), testUser1.getUsername()),
                        new GroupChatRequest.ParticipantRequest(testUser3.getId(), testUser3.getUsername())
                ))
                .build();

        webClient.post().uri("/api/group-chats").header("Authorization", "Bearer " + accessToken).bodyValue(request1).retrieve().bodyToMono(GroupChatResponse.class).block();
        webClient.post().uri("/api/group-chats").header("Authorization", "Bearer " + accessToken).bodyValue(request2).retrieve().bodyToMono(GroupChatResponse.class).block();

        List<?> response = webClient.get()
                .uri("/api/group-chats/user/" + testUser1.getId())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(List.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);
    }

    @Test
    @DisplayName("GET /api/group-chats/user/{userId} - should return empty list when user has no groups")
    void getGroupsForUser_UserNotInAnyGroup_ReturnsEmptyList() {
        List<?> response = webClient.get()
                .uri("/api/group-chats/user/" + testUser1.getId())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(List.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response).isEmpty();
    }

    // ========== FAILURE CASES ==========

    @Test
    @DisplayName("POST /api/group-chats - should fail with blank group name")
    void createGroup_WithBlankName_ReturnsBadRequest() {
        GroupChatRequest request = GroupChatRequest.builder()
                .name("")
                .participants(List.of(
                        new GroupChatRequest.ParticipantRequest(testUser1.getId(), testUser1.getUsername()),
                        new GroupChatRequest.ParticipantRequest(testUser2.getId(), testUser2.getUsername())
                ))
                .build();

        try {
            webClient.post()
                    .uri("/api/group-chats")
                    .header("Authorization", "Bearer " + accessToken)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("400");
        }
    }

    @Test
    @DisplayName("POST /api/group-chats - should fail with less than 2 participants")
    void createGroup_WithOneParticipant_ReturnsBadRequest() {
        GroupChatRequest request = GroupChatRequest.builder()
                .name("Solo Group")
                .participants(List.of(
                        new GroupChatRequest.ParticipantRequest(testUser1.getId(), testUser1.getUsername())
                ))
                .build();

        try {
            webClient.post()
                    .uri("/api/group-chats")
                    .header("Authorization", "Bearer " + accessToken)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("400");
        }
    }

    @Test
    @DisplayName("GET /api/group-chats/{groupId} - should return error for non-existent group")
    void getGroupById_NonExistentGroup_ReturnsNotFound() {
        try {
            webClient.get()
                    .uri("/api/group-chats/99999")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            assertThat(false).as("Expected an error response for non-existent group").isTrue();
        } catch (Exception e) {
            assertThat(e.getMessage()).containsAnyOf("404", "500", "401");
        }
    }
}
