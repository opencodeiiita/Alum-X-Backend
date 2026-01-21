package com.opencode.alumxbackend.connection.controller;

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
import com.opencode.alumxbackend.connection.model.Connection;
import com.opencode.alumxbackend.connection.model.ConnectionStatus;
import com.opencode.alumxbackend.connection.repository.ConnectionRepository;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.model.UserRole;
import com.opencode.alumxbackend.users.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ConnectionControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private WebClient webClient;
    private User sender;
    private User receiver;
    private String senderToken;
    private String receiverToken;

    @BeforeEach
    void setUp() {
        webClient = WebClient.create("http://localhost:" + port);
        
        connectionRepository.deleteAll();
        userRepository.deleteAll();

        sender = User.builder()
                .username("sender")
                .name("Sender User")
                .email("sender@test.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.STUDENT)
                .profileCompleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        sender = userRepository.save(sender);

        receiver = User.builder()
                .username("receiver")
                .name("Receiver User")
                .email("receiver@test.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.ALUMNI)
                .profileCompleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        receiver = userRepository.save(receiver);

        senderToken = login("sender@test.com", "password123");
        receiverToken = login("receiver@test.com", "password123");
    }

    private String login(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);
        LoginResponse loginResponse = webClient.post()
                .uri("/api/auth/login")
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .block();
        return loginResponse.getAccessToken();
    }


    private Connection createPendingConnection() {
        Connection connection = Connection.builder()
                .senderId(sender.getId())
                .receiverId(receiver.getId())
                .status(ConnectionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        return connectionRepository.save(connection);
    }

    // ========== ACCEPT CONNECTION TESTS ==========

    @Test
    @DisplayName("POST /api/connections/{id}/accept - receiver can accept pending request")
    void acceptConnection_AsReceiver_Success() {
        Connection connection = createPendingConnection();

        String response = webClient.post()
                .uri("/api/connections/" + connection.getId() + "/accept")
                .header("Authorization", "Bearer " + receiverToken)
                .header("X-USER-ID", receiver.getId().toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertThat(response).contains("accepted");

        Connection updated = connectionRepository.findById(connection.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ConnectionStatus.ACCEPTED);
    }

    @Test
    @DisplayName("POST /api/connections/{id}/accept - sender cannot accept their own request")
    void acceptConnection_AsSender_Fails() {
        Connection connection = createPendingConnection();

        try {
            webClient.post()
                    .uri("/api/connections/" + connection.getId() + "/accept")
                    .header("Authorization", "Bearer " + senderToken)
                    .header("X-USER-ID", sender.getId().toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            assertThat(false).as("Expected error when sender tries to accept").isTrue();
        } catch (Exception e) {
            assertThat(e.getMessage()).containsAnyOf("400", "401", "403", "500");
        }

        Connection unchanged = connectionRepository.findById(connection.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(ConnectionStatus.PENDING);
    }

    @Test
    @DisplayName("POST /api/connections/{id}/accept - cannot accept non-existent connection")
    void acceptConnection_NonExistent_Fails() {
        try {
            webClient.post()
                    .uri("/api/connections/99999/accept")
                    .header("Authorization", "Bearer " + receiverToken)
                    .header("X-USER-ID", receiver.getId().toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            assertThat(false).as("Expected error for non-existent connection").isTrue();
        } catch (Exception e) {
            assertThat(e.getMessage()).containsAnyOf("404", "401", "500");
        }
    }

    @Test
    @DisplayName("POST /api/connections/{id}/accept - cannot accept already accepted connection")
    void acceptConnection_AlreadyAccepted_Fails() {
        Connection connection = createPendingConnection();
        connection.setStatus(ConnectionStatus.ACCEPTED);
        connectionRepository.save(connection);

        try {
            webClient.post()
                    .uri("/api/connections/" + connection.getId() + "/accept")
                    .header("Authorization", "Bearer " + receiverToken)
                    .header("X-USER-ID", receiver.getId().toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            assertThat(false).as("Expected error for already accepted connection").isTrue();
        } catch (Exception e) {
            assertThat(e.getMessage()).containsAnyOf("400", "401", "500");
        }
    }


    // ========== REJECT CONNECTION TESTS ==========

    @Test
    @DisplayName("POST /api/connections/{id}/reject - receiver can reject pending request")
    void rejectConnection_AsReceiver_Success() {
        Connection connection = createPendingConnection();

        String response = webClient.post()
                .uri("/api/connections/" + connection.getId() + "/reject")
                .header("Authorization", "Bearer " + receiverToken)
                .header("X-USER-ID", receiver.getId().toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertThat(response).contains("rejected");

        Connection updated = connectionRepository.findById(connection.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ConnectionStatus.REJECTED);
    }

    @Test
    @DisplayName("POST /api/connections/{id}/reject - sender cannot reject their own request")
    void rejectConnection_AsSender_Fails() {
        Connection connection = createPendingConnection();

        try {
            webClient.post()
                    .uri("/api/connections/" + connection.getId() + "/reject")
                    .header("Authorization", "Bearer " + senderToken)
                    .header("X-USER-ID", sender.getId().toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            assertThat(false).as("Expected error when sender tries to reject").isTrue();
        } catch (Exception e) {
            assertThat(e.getMessage()).containsAnyOf("400", "401", "403", "500");
        }

        Connection unchanged = connectionRepository.findById(connection.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(ConnectionStatus.PENDING);
    }

    @Test
    @DisplayName("POST /api/connections/{id}/reject - cannot reject already rejected connection")
    void rejectConnection_AlreadyRejected_Fails() {
        Connection connection = createPendingConnection();
        connection.setStatus(ConnectionStatus.REJECTED);
        connectionRepository.save(connection);

        try {
            webClient.post()
                    .uri("/api/connections/" + connection.getId() + "/reject")
                    .header("Authorization", "Bearer " + receiverToken)
                    .header("X-USER-ID", receiver.getId().toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            assertThat(false).as("Expected error for already rejected connection").isTrue();
        } catch (Exception e) {
            assertThat(e.getMessage()).containsAnyOf("400", "401", "500");
        }
    }

    // ========== CANCEL CONNECTION TESTS ==========

    @Test
    @DisplayName("POST /api/connections/{id}/cancel - sender can cancel pending request")
    void cancelConnection_AsSender_Success() {
        Connection connection = createPendingConnection();

        String response = webClient.post()
                .uri("/api/connections/" + connection.getId() + "/cancel")
                .header("Authorization", "Bearer " + senderToken)
                .header("X-USER-ID", sender.getId().toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertThat(response).contains("cancelled");

        assertThat(connectionRepository.findById(connection.getId())).isEmpty();
    }

    @Test
    @DisplayName("POST /api/connections/{id}/cancel - receiver cannot cancel request")
    void cancelConnection_AsReceiver_Fails() {
        Connection connection = createPendingConnection();

        try {
            webClient.post()
                    .uri("/api/connections/" + connection.getId() + "/cancel")
                    .header("Authorization", "Bearer " + receiverToken)
                    .header("X-USER-ID", receiver.getId().toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            assertThat(false).as("Expected error when receiver tries to cancel").isTrue();
        } catch (Exception e) {
            assertThat(e.getMessage()).containsAnyOf("400", "401", "403", "500");
        }

        Connection unchanged = connectionRepository.findById(connection.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(ConnectionStatus.PENDING);
    }

    @Test
    @DisplayName("POST /api/connections/{id}/cancel - cannot cancel accepted connection")
    void cancelConnection_AlreadyAccepted_Fails() {
        Connection connection = createPendingConnection();
        connection.setStatus(ConnectionStatus.ACCEPTED);
        connectionRepository.save(connection);

        try {
            webClient.post()
                    .uri("/api/connections/" + connection.getId() + "/cancel")
                    .header("Authorization", "Bearer " + senderToken)
                    .header("X-USER-ID", sender.getId().toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            assertThat(false).as("Expected error for already accepted connection").isTrue();
        } catch (Exception e) {
            assertThat(e.getMessage()).containsAnyOf("400", "401", "500");
        }

        Connection unchanged = connectionRepository.findById(connection.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(ConnectionStatus.ACCEPTED);
    }

    @Test
    @DisplayName("POST /api/connections/{id}/cancel - cannot cancel non-existent connection")
    void cancelConnection_NonExistent_Fails() {
        try {
            webClient.post()
                    .uri("/api/connections/99999/cancel")
                    .header("Authorization", "Bearer " + senderToken)
                    .header("X-USER-ID", sender.getId().toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            assertThat(false).as("Expected error for non-existent connection").isTrue();
        } catch (Exception e) {
            assertThat(e.getMessage()).containsAnyOf("404", "401", "500");
        }
    }

    // ========== FETCH CONNECTIONS TESTS ==========

    @Test
    @DisplayName("GET /api/connections/pending/received - returns pending requests for receiver")
    void getPendingReceivedRequests_Success() {
        createPendingConnection();

        List<?> response = webClient.get()
                .uri("/api/connections/pending/received")
                .header("Authorization", "Bearer " + receiverToken)
                .header("X-USER-ID", receiver.getId().toString())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
    }

    @Test
    @DisplayName("GET /api/connections/pending/sent - returns pending requests sent by user")
    void getPendingSentRequests_Success() {
        createPendingConnection();

        List<?> response = webClient.get()
                .uri("/api/connections/pending/sent")
                .header("Authorization", "Bearer " + senderToken)
                .header("X-USER-ID", sender.getId().toString())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
    }

    @Test
    @DisplayName("GET /api/connections/accepted - returns accepted connections")
    void getAcceptedConnections_Success() {
        Connection connection = createPendingConnection();
        connection.setStatus(ConnectionStatus.ACCEPTED);
        connectionRepository.save(connection);

        List<?> response = webClient.get()
                .uri("/api/connections/accepted")
                .header("Authorization", "Bearer " + senderToken)
                .header("X-USER-ID", sender.getId().toString())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
    }

    @Test
    @DisplayName("GET /api/connections/accepted - returns empty list when no connections")
    void getAcceptedConnections_Empty() {
        List<?> response = webClient.get()
                .uri("/api/connections/accepted")
                .header("Authorization", "Bearer " + senderToken)
                .header("X-USER-ID", sender.getId().toString())
                .retrieve()
                .bodyToMono(List.class)
                .block();

        assertThat(response).isNotNull();
        assertThat(response).isEmpty();
    }
}
