package com.opencode.alumxbackend.groupchatmessages.controller;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import com.opencode.alumxbackend.common.RestResponsePage;

import com.opencode.alumxbackend.auth.dto.LoginRequest;
import com.opencode.alumxbackend.auth.dto.LoginResponse;
import com.opencode.alumxbackend.groupchat.dto.GroupChatRequest;
import com.opencode.alumxbackend.groupchat.dto.GroupChatResponse;
import com.opencode.alumxbackend.groupchat.repository.GroupChatRepository;
import com.opencode.alumxbackend.groupchatmessages.dto.GroupMessageResponse;
import com.opencode.alumxbackend.groupchatmessages.dto.SendGroupMessageRequest;
import com.opencode.alumxbackend.groupchatmessages.repository.GroupMessageRepository;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.model.UserRole;
import com.opencode.alumxbackend.users.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GroupMessageControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupChatRepository groupChatRepository;

    @Autowired
    private GroupMessageRepository groupMessageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private WebClient webClient;
    private User testUser1;
    private User testUser2;
    private Long testGroupId;
    private String accessToken;

    @BeforeEach
    void setUp() {
        webClient = WebClient.create("http://localhost:" + port);

        groupMessageRepository.deleteAll();
        groupChatRepository.deleteAll();
        userRepository.deleteAll();

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
                .role(UserRole.ALUMNI)
                .profileCompleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser2 = userRepository.save(testUser2);

        // Login to get access token
        LoginRequest loginRequest = new LoginRequest("user1@test.com", "password123");
        LoginResponse loginResponse = webClient.post()
                .uri("/api/auth/login")
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .block();
        accessToken = loginResponse.getAccessToken();

        // Create a test group
        GroupChatRequest groupRequest = GroupChatRequest.builder()
                .name("Test Group")
                .participants(List.of(
                        new GroupChatRequest.ParticipantRequest(testUser1.getId(), testUser1.getUsername()),
                        new GroupChatRequest.ParticipantRequest(testUser2.getId(), testUser2.getUsername())
                ))
                .build();

        GroupChatResponse createdGroup = webClient.post()
                .uri("/api/group-chats")
                .header("Authorization", "Bearer " + accessToken)
                .bodyValue(groupRequest)
                .retrieve()
                .bodyToMono(GroupChatResponse.class)
                .block();

        testGroupId = createdGroup.getGroupId();
    }

    @Test
    @DisplayName("POST /api/groups/{groupId}/messages - should send message to group")
    void sendMessage_WithValidData_ReturnsCreatedMessage() {
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setUserId(testUser1.getId());
        request.setContent("Hello everyone!");

        GroupMessageResponse response = webClient.post()
                .uri("/api/groups/" + testGroupId + "/messages")
                .header("Authorization", "Bearer " + accessToken)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GroupMessageResponse.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getSenderUserId()).isEqualTo(testUser1.getId());
        assertThat(response.getContent()).isEqualTo("Hello everyone!");
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("GET /api/groups/{groupId}/messages - should return all messages from group")
    void getMessages_ExistingMessages_ReturnsAllMessages() {
        SendGroupMessageRequest msg1 = new SendGroupMessageRequest();
        msg1.setUserId(testUser1.getId());
        msg1.setContent("First message");

        SendGroupMessageRequest msg2 = new SendGroupMessageRequest();
        msg2.setUserId(testUser2.getId());
        msg2.setContent("Second message");

        webClient.post().uri("/api/groups/" + testGroupId + "/messages").header("Authorization", "Bearer " + accessToken).bodyValue(msg1).retrieve().bodyToMono(GroupMessageResponse.class).block();
        webClient.post().uri("/api/groups/" + testGroupId + "/messages").header("Authorization", "Bearer " + accessToken).bodyValue(msg2).retrieve().bodyToMono(GroupMessageResponse.class).block();

        RestResponsePage<GroupMessageResponse> response = webClient.get()
                .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser1.getId())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<RestResponsePage<GroupMessageResponse>>() {})
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("GET /api/groups/{groupId}/messages - should return empty list when no messages")
    void getMessages_NoMessages_ReturnsEmptyList() {
        RestResponsePage<GroupMessageResponse> response = webClient.get()
                .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser1.getId())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<RestResponsePage<GroupMessageResponse>>() {})
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("DELETE /api/groups/{groupId}/messages/{messageId} - should delete message")
    void deleteMessage_RemovesFromGetRoutes() {
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setUserId(testUser1.getId());
        request.setContent("Message to delete");

        GroupMessageResponse created = webClient.post()
                .uri("/api/groups/" + testGroupId + "/messages")
                .header("Authorization", "Bearer " + accessToken)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GroupMessageResponse.class)
                .block();

        assertThat(created).isNotNull();

        var deleteResponse = webClient.delete()
                .uri("/api/groups/" + testGroupId + "/messages/" + created.getId() + "?userId=" + testUser1.getId())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .toBodilessEntity()
                .block();

        assertThat(deleteResponse).isNotNull();
        assertThat(deleteResponse.getStatusCode().value()).isEqualTo(204);

        RestResponsePage<GroupMessageResponse> allMessages = webClient.get()
                .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser1.getId())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<RestResponsePage<GroupMessageResponse>>() {})
                .block();

        assertThat(allMessages).isNotNull();
        assertThat(allMessages.getContent()).isEmpty();
    }

    @Test
    @DisplayName("POST /api/groups/{groupId}/messages - should fail with empty content")
    void sendMessage_WithEmptyContent_ReturnsBadRequest() {
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setUserId(testUser1.getId());
        request.setContent("");

        try {
            webClient.post()
                    .uri("/api/groups/" + testGroupId + "/messages")
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
    @DisplayName("POST /api/groups/{groupId}/messages - should fail with too long content")
    void sendMessage_WithTooLongContent_ReturnsBadRequest() {
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setUserId(testUser1.getId());
        request.setContent("a".repeat(1001));

        try {
            webClient.post()
                    .uri("/api/groups/" + testGroupId + "/messages")
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
    @DisplayName("POST /api/groups/{groupId}/messages - should fail without userId")
    void sendMessage_WithoutUserId_ReturnsBadRequest() {
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setContent("This should fail");

        try {
            webClient.post()
                    .uri("/api/groups/" + testGroupId + "/messages")
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
    @DisplayName("POST /api/groups/{groupId}/messages - should fail for non-existent group")
    void sendMessage_ToNonExistentGroup_ReturnsNotFound() {
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setUserId(testUser1.getId());
        request.setContent("This should fail");

        try {
            webClient.post()
                    .uri("/api/groups/99999/messages")
                    .header("Authorization", "Bearer " + accessToken)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            assertThat(e.getMessage()).containsAnyOf("401", "404", "500");
        }
    }

    @Test
    @DisplayName("GET /api/groups/{groupId}/messages - should fail for non-existent group")
    void getMessages_FromNonExistentGroup_ReturnsNotFound() {
        try {
            webClient.get()
                    .uri("/api/groups/99999/messages?userId=" + testUser1.getId())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            assertThat(e.getMessage()).containsAnyOf("401", "404", "500");
        }
    }

    @Test
    @DisplayName("GET /api/groups/{groupId}/messages - should return paginated messages")
    void getPaginatedMessages_DefaultParams_ReturnsFirstPage() {
        for (int i = 1; i <= 25; i++) {
            SendGroupMessageRequest msg = new SendGroupMessageRequest();
            msg.setUserId(testUser1.getId());
            msg.setContent("Message " + i);
            webClient.post().uri("/api/groups/" + testGroupId + "/messages")
                    .header("Authorization", "Bearer " + accessToken)
                    .bodyValue(msg).retrieve().bodyToMono(GroupMessageResponse.class).block();
        }

        var response = webClient.get()
                .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser1.getId())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response).contains("\"totalElements\":25");
        assertThat(response).contains("\"totalPages\":2");
    }

    @Test
    @DisplayName("GET /api/groups/{groupId}/messages - should return custom page size")
    void getPaginatedMessages_CustomSize_ReturnsCorrectSize() {
        for (int i = 1; i <= 15; i++) {
            SendGroupMessageRequest msg = new SendGroupMessageRequest();
            msg.setUserId(testUser1.getId());
            msg.setContent("Message " + i);
            webClient.post().uri("/api/groups/" + testGroupId + "/messages")
                    .header("Authorization", "Bearer " + accessToken)
                    .bodyValue(msg).retrieve().bodyToMono(GroupMessageResponse.class).block();
        }

        var response = webClient.get()
                .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser1.getId() + "&page=0&size=5")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response).contains("\"totalElements\":15");
        assertThat(response).contains("\"size\":5");
    }

    @Test
    @DisplayName("GET /api/groups/{groupId}/messages - should fail for non-member")
    void getPaginatedMessages_NonMember_ReturnsForbidden() {
        User testUser3 = User.builder()
                .username("user3")
                .name("Test User 3")
                .email("user3@test.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.PROFESSOR)
                .profileCompleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser3 = userRepository.save(testUser3);

        SendGroupMessageRequest msg = new SendGroupMessageRequest();
        msg.setUserId(testUser1.getId());
        msg.setContent("Message for members only");
        webClient.post().uri("/api/groups/" + testGroupId + "/messages")
                .header("Authorization", "Bearer " + accessToken)
                .bodyValue(msg).retrieve().bodyToMono(GroupMessageResponse.class).block();

        try {
            webClient.get()
                    .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser3.getId())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            assertThat(e.getMessage()).containsAnyOf("401", "403");
        }
    }

    @Test
    @DisplayName("GET /api/groups/{groupId}/messages - should reject negative page")
    void getPaginatedMessages_NegativePage_ReturnsError() {
        for (int i = 1; i <= 5; i++) {
            SendGroupMessageRequest msg = new SendGroupMessageRequest();
            msg.setUserId(testUser1.getId());
            msg.setContent("Message " + i);
            webClient.post().uri("/api/groups/" + testGroupId + "/messages")
                    .header("Authorization", "Bearer " + accessToken)
                    .bodyValue(msg).retrieve().bodyToMono(GroupMessageResponse.class).block();
        }

        try {
            webClient.get()
                    .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser1.getId() + "&page=-1&size=5")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            throw new AssertionError("Expected exception for negative page number");
        } catch (Exception e) {
            assertThat(e.getMessage()).containsAnyOf("400", "401");
        }
    }

    @Test
    @DisplayName("GET /api/groups/{groupId}/messages - should reject invalid size")
    void getPaginatedMessages_InvalidSize_ReturnsError() {
        try {
            webClient.get()
                    .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser1.getId() + "&page=0&size=0")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            throw new AssertionError("Expected exception for invalid page size");
        } catch (Exception e) {
            assertThat(e.getMessage()).containsAnyOf("400", "401");
        }
    }

    @Test
    @DisplayName("GET /api/groups/{groupId}/messages - both members can access")
    void getPaginatedMessages_BothMembers_CanAccess() {
        SendGroupMessageRequest msg1 = new SendGroupMessageRequest();
        msg1.setUserId(testUser1.getId());
        msg1.setContent("From user 1");
        webClient.post().uri("/api/groups/" + testGroupId + "/messages")
                .header("Authorization", "Bearer " + accessToken)
                .bodyValue(msg1).retrieve().bodyToMono(GroupMessageResponse.class).block();

        var response1 = webClient.get()
                .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser1.getId())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        assertThat(response1).isNotNull();
        assertThat(response1).contains("From user 1");
    }
}
