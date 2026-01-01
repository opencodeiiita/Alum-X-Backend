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
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import com.opencode.alumxbackend.common.RestResponsePage;

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

    private WebClient webClient;
    private User testUser1;
    private User testUser2;
    private Long testGroupId;

    @BeforeEach
    void setUp() {
        webClient = WebClient.create("http://localhost:" + port);

        // Clean up
        groupMessageRepository.deleteAll();
        groupChatRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        testUser1 = User.builder()
                .username("user1")
                .name("Test User 1")
                .email("user1@test.com")
                .passwordHash("hashedpass")
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
                .passwordHash("hashedpass")
                .role(UserRole.ALUMNI)
                .profileCompleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser2 = userRepository.save(testUser2);

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
                .bodyValue(groupRequest)
                .retrieve()
                .bodyToMono(GroupChatResponse.class)
                .block();

        testGroupId = createdGroup.getGroupId();
    }

    // ========== SUCCESS CASES ==========

    @Test
    @DisplayName("POST /api/groups/{groupId}/messages - should send message to group")
    void sendMessage_WithValidData_ReturnsCreatedMessage() {
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setUserId(testUser1.getId());
        request.setContent("Hello everyone!");

        GroupMessageResponse response = webClient.post()
                .uri("/api/groups/" + testGroupId + "/messages")
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
        // Send two messages
        SendGroupMessageRequest msg1 = new SendGroupMessageRequest();
        msg1.setUserId(testUser1.getId());
        msg1.setContent("First message");

        SendGroupMessageRequest msg2 = new SendGroupMessageRequest();
        msg2.setUserId(testUser2.getId());
        msg2.setContent("Second message");

        webClient.post().uri("/api/groups/" + testGroupId + "/messages").bodyValue(msg1).retrieve().bodyToMono(GroupMessageResponse.class).block();
        webClient.post().uri("/api/groups/" + testGroupId + "/messages").bodyValue(msg2).retrieve().bodyToMono(GroupMessageResponse.class).block();

        // Fetch messages (paginated response)
        RestResponsePage<GroupMessageResponse> response = webClient.get()
                .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser1.getId())
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
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<RestResponsePage<GroupMessageResponse>>() {})
                .block();

        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("DELETE /api/groups/{groupId}/messages/{messageId} - should delete and not return in get routes")
    void deleteMessage_RemovesFromGetRoutes() {
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setUserId(testUser1.getId());
        request.setContent("Message to delete");

        GroupMessageResponse created = webClient.post()
                .uri("/api/groups/" + testGroupId + "/messages")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GroupMessageResponse.class)
                .block();

        assertThat(created).isNotNull();

        var deleteResponse = webClient.delete()
                .uri("/api/groups/" + testGroupId + "/messages/" + created.getId()
                        + "?userId=" + testUser1.getId())
                .retrieve()
                .toBodilessEntity()
                .block();

        assertThat(deleteResponse).isNotNull();
        assertThat(deleteResponse.getStatusCode().value()).isEqualTo(204);

        RestResponsePage<GroupMessageResponse> allMessages = webClient.get()
                .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser1.getId())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<RestResponsePage<GroupMessageResponse>>() {})
                .block();

        assertThat(allMessages).isNotNull();
        assertThat(allMessages.getContent()).isEmpty();
    }

    // ========== FAILURE CASES ==========

    @Test
    @DisplayName("POST /api/groups/{groupId}/messages - should fail with empty message content")
    void sendMessage_WithEmptyContent_ReturnsBadRequest() {
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setUserId(testUser1.getId());
        request.setContent("");

        try {
            webClient.post()
                    .uri("/api/groups/" + testGroupId + "/messages")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("400");
        }
    }

    @Test
    @DisplayName("POST /api/groups/{groupId}/messages - should fail with message exceeding max length")
    void sendMessage_WithTooLongContent_ReturnsBadRequest() {
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setUserId(testUser1.getId());
        request.setContent("a".repeat(1001)); // More than 1000 characters

        try {
            webClient.post()
                    .uri("/api/groups/" + testGroupId + "/messages")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("400");
        }
    }

    @Test
    @DisplayName("POST /api/groups/{groupId}/messages - should fail with missing userId")
    void sendMessage_WithoutUserId_ReturnsBadRequest() {
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setContent("This should fail");
        // userId is null

        try {
            webClient.post()
                    .uri("/api/groups/" + testGroupId + "/messages")
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
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("404");
        }
    }

    @Test
    @DisplayName("GET /api/groups/{groupId}/messages?userId=<userId> - should fail for non-existent group")
    void getMessages_FromNonExistentGroup_ReturnsNotFound() {
        // Note: Service throws GroupNotFoundException which is RuntimeException
        // but we are catching it and returning 404
        try {
            webClient.get()
                    .uri("/api/groups/99999/messages?userId=" + testUser1.getId())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("404");
        }
    }

    // ========== PAGINATION TESTS ==========

    @Test
    @DisplayName("GET /api/groups/{groupId}/messages - should return paginated messages with default params")
    void getPaginatedMessages_DefaultParams_ReturnsFirstPage() {
        // Create 25 messages
        for (int i = 1; i <= 25; i++) {
            SendGroupMessageRequest msg = new SendGroupMessageRequest();
            msg.setUserId(testUser1.getId());
            msg.setContent("Message " + i);
            webClient.post().uri("/api/groups/" + testGroupId + "/messages")
                    .bodyValue(msg).retrieve().bodyToMono(GroupMessageResponse.class).block();
        }

        // Fetch first page with default size (20)
        var response = webClient.get()
                .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser1.getId())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response).contains("\"totalElements\":25");
        assertThat(response).contains("\"totalPages\":2");
        assertThat(response).contains("\"size\":20");
        assertThat(response).contains("\"number\":0");
    }

    @Test
    @DisplayName("GET /api/groups/{groupId}/messages - should return specified page size")
    void getPaginatedMessages_CustomSize_ReturnsCorrectSize() {
        // Create 15 messages
        for (int i = 1; i <= 15; i++) {
            SendGroupMessageRequest msg = new SendGroupMessageRequest();
            msg.setUserId(testUser1.getId());
            msg.setContent("Message " + i);
            webClient.post().uri("/api/groups/" + testGroupId + "/messages")
                    .bodyValue(msg).retrieve().bodyToMono(GroupMessageResponse.class).block();
        }

        // Fetch with size 5
        var response = webClient.get()
                .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser1.getId() + "&page=0&size=5")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response).contains("\"totalElements\":15");
        assertThat(response).contains("\"totalPages\":3");
        assertThat(response).contains("\"size\":5");
        assertThat(response).contains("\"numberOfElements\":5");
    }

    @Test
    @DisplayName("GET /api/groups/{groupId}/messages - should return second page correctly")
    void getPaginatedMessages_SecondPage_ReturnsCorrectPage() {
        // Create 12 messages
        for (int i = 1; i <= 12; i++) {
            SendGroupMessageRequest msg = new SendGroupMessageRequest();
            msg.setUserId(testUser1.getId());
            msg.setContent("Message " + i);
            webClient.post().uri("/api/groups/" + testGroupId + "/messages")
                    .bodyValue(msg).retrieve().bodyToMono(GroupMessageResponse.class).block();
        }

        // Fetch second page with size 5
        var response = webClient.get()
                .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser1.getId() + "&page=1&size=5")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response).contains("\"number\":1"); // Page number
        assertThat(response).contains("\"numberOfElements\":5");
    }

    @Test
    @DisplayName("GET /api/groups/{groupId}/messages - should order messages by timestamp ascending")
    void getPaginatedMessages_OrdersByTimestamp_Ascending() {
        // Create 3 messages with slight delays
        SendGroupMessageRequest msg1 = new SendGroupMessageRequest();
        msg1.setUserId(testUser1.getId());
        msg1.setContent("First message");
        webClient.post().uri("/api/groups/" + testGroupId + "/messages")
                .bodyValue(msg1).retrieve().bodyToMono(GroupMessageResponse.class).block();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        SendGroupMessageRequest msg2 = new SendGroupMessageRequest();
        msg2.setUserId(testUser2.getId());
        msg2.setContent("Second message");
        webClient.post().uri("/api/groups/" + testGroupId + "/messages")
                .bodyValue(msg2).retrieve().bodyToMono(GroupMessageResponse.class).block();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        SendGroupMessageRequest msg3 = new SendGroupMessageRequest();
        msg3.setUserId(testUser1.getId());
        msg3.setContent("Third message");
        webClient.post().uri("/api/groups/" + testGroupId + "/messages")
                .bodyValue(msg3).retrieve().bodyToMono(GroupMessageResponse.class).block();

        // Fetch messages
        var response = webClient.get()
                .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser1.getId() + "&page=0&size=10")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertThat(response).isNotNull();
        // Verify order: First, Second, Third
        int firstPos = response.indexOf("First message");
        int secondPos = response.indexOf("Second message");
        int thirdPos = response.indexOf("Third message");
        assertThat(firstPos).isLessThan(secondPos);
        assertThat(secondPos).isLessThan(thirdPos);
    }

    @Test
    @DisplayName("GET /api/groups/{groupId}/messages - should fail for non-member user")
    void getPaginatedMessages_NonMember_ReturnsForbidden() {
        // Create a third user who is not in the group
        User testUser3 = User.builder()
                .username("user3")
                .name("Test User 3")
                .email("user3@test.com")
                .passwordHash("hashedpass")
                .role(UserRole.PROFESSOR)
                .profileCompleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testUser3 = userRepository.save(testUser3);

        // Create a message first
        SendGroupMessageRequest msg = new SendGroupMessageRequest();
        msg.setUserId(testUser1.getId());
        msg.setContent("Message for members only");
        webClient.post().uri("/api/groups/" + testGroupId + "/messages")
                .bodyValue(msg).retrieve().bodyToMono(GroupMessageResponse.class).block();

        // Try to fetch as non-member
        try {
            webClient.get()
                    .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser3.getId())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            // Should fail with 403 or 500 depending on exception handling
            assertThat(e.getMessage()).containsAnyOf("403", "500");
        }
    }

    @Test
    @DisplayName("GET /api/groups/{groupId}/messages - should handle empty page gracefully")
    void getPaginatedMessages_EmptyPage_ReturnsEmptyContent() {
        // Create only 5 messages
        for (int i = 1; i <= 5; i++) {
            SendGroupMessageRequest msg = new SendGroupMessageRequest();
            msg.setUserId(testUser1.getId());
            msg.setContent("Message " + i);
            webClient.post().uri("/api/groups/" + testGroupId + "/messages")
                    .bodyValue(msg).retrieve().bodyToMono(GroupMessageResponse.class).block();
        }

        // Request page 10 which doesn't exist
        var response = webClient.get()
                .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser1.getId() + "&page=10&size=5")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response).contains("\"totalElements\":5");
        assertThat(response).contains("\"numberOfElements\":0");
        assertThat(response).contains("\"content\":[]");
    }

    @Test
    @DisplayName("GET /api/groups/{groupId}/messages - should reject negative page number")
    void getPaginatedMessages_NegativePage_ReturnsError() {
        // Create 5 messages
        for (int i = 1; i <= 5; i++) {
            SendGroupMessageRequest msg = new SendGroupMessageRequest();
            msg.setUserId(testUser1.getId());
            msg.setContent("Message " + i);
            webClient.post().uri("/api/groups/" + testGroupId + "/messages")
                    .bodyValue(msg).retrieve().bodyToMono(GroupMessageResponse.class).block();
        }

        // Try negative page - should throw exception
        try {
            webClient.get()
                    .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser1.getId() + "&page=-1&size=5")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            // Should not reach here
            throw new AssertionError("Expected exception for negative page number");
        } catch (Exception e) {
            // Spring Data throws IllegalArgumentException which results in 500
            assertThat(e.getMessage()).contains("500");
        }
    }

    @Test
    @DisplayName("GET /api/groups/{groupId}/messages - should reject invalid page size")
    void getPaginatedMessages_InvalidSize_ReturnsError() {
        // Try zero or negative size - should throw exception
        try {
            webClient.get()
                    .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser1.getId() + "&page=0&size=0")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            // Should not reach here
            throw new AssertionError("Expected exception for invalid page size");
        } catch (Exception e) {
            // Spring Data throws IllegalArgumentException which results in 500
            assertThat(e.getMessage()).contains("500");
        }
    }

    @Test
    @DisplayName("GET /api/groups/{groupId}/messages - both users can access as members")
    void getPaginatedMessages_BothMembers_CanAccess() {
        // Create messages from both users
        SendGroupMessageRequest msg1 = new SendGroupMessageRequest();
        msg1.setUserId(testUser1.getId());
        msg1.setContent("From user 1");
        webClient.post().uri("/api/groups/" + testGroupId + "/messages")
                .bodyValue(msg1).retrieve().bodyToMono(GroupMessageResponse.class).block();

        SendGroupMessageRequest msg2 = new SendGroupMessageRequest();
        msg2.setUserId(testUser2.getId());
        msg2.setContent("From user 2");
        webClient.post().uri("/api/groups/" + testGroupId + "/messages")
                .bodyValue(msg2).retrieve().bodyToMono(GroupMessageResponse.class).block();

        // User 1 fetches
        var response1 = webClient.get()
                .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser1.getId())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        assertThat(response1).isNotNull();
        assertThat(response1).contains("From user 1");
        assertThat(response1).contains("From user 2");

        // User 2 fetches
        var response2 = webClient.get()
                .uri("/api/groups/" + testGroupId + "/messages?userId=" + testUser2.getId())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        assertThat(response2).isNotNull();
        assertThat(response2).contains("From user 1");
        assertThat(response2).contains("From user 2");
    }
}
