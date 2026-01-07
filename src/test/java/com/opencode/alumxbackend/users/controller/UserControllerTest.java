package com.opencode.alumxbackend.users.controller;

import com.opencode.alumxbackend.auth.dto.LoginRequest;
import com.opencode.alumxbackend.auth.dto.LoginResponse;
import com.opencode.alumxbackend.jobposts.repository.CommentRepository;
import com.opencode.alumxbackend.notifications.repository.NotificationRepository;
import com.opencode.alumxbackend.users.dto.UserProfileResponse;
import com.opencode.alumxbackend.users.dto.UserProfileUpdateRequest;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT )
@ActiveProfiles("test")
public class UserControllerTest {

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

    private String authTokenUser1;


    @BeforeEach
    void setup(){

        webClient = WebClient.create("http://localhost:"+port);

        // Clean up dependent entities first to avoid foreign key constraint violations
        commentRepository.deleteAll();
        notificationRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .username("gaurav63")
                .email("ife2022004@iiita.ac.in")
                .name("Gaurav Chhetri")
                .currentCompany("Gromo")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.STUDENT)
                .profileCompleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUser = userRepository.save(testUser);

        LoginRequest loginRequest = new LoginRequest("gaurav63","password");
        LoginResponse loginResponse = webClient.post()
                .uri("/api/auth/login")
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .block();

        authTokenUser1 = loginResponse.getAccessToken();
    }


    @Test
    @DisplayName("should display user profile")
    void shoudShowUserProfile(){

        UserProfileResponse userProfileResponse = webClient.get()
                .uri("/api/users/"+testUser.getId()+"/profile")
                .header("Authorization","Bearer "+authTokenUser1)
                .retrieve()
                .bodyToMono(UserProfileResponse.class)
                .block();

        assertThat(userProfileResponse).isNotNull();
        System.out.println("UserProfileResponse = " + userProfileResponse);

    }


    @Test
    @DisplayName("should update the profile details for a user")
    void shouldUpdateProfileDetailsOfAUser(){

        UserProfileUpdateRequest userProfileUpdateRequest = new UserProfileUpdateRequest();
        userProfileUpdateRequest.setCurrentCompany("DealShare");


        UserProfileResponse userProfileUpdateRequest1 = webClient.patch()
                .uri("/api/users/"+testUser.getId()+"/profile")
                .bodyValue(userProfileUpdateRequest)
                .header("Authorization","Bearer "+authTokenUser1)
                .retrieve()
                .bodyToMono(UserProfileResponse.class)
                .block();


        assertThat(userProfileUpdateRequest1).isNotNull();
        assert userProfileUpdateRequest1 != null;
        assertThat(userProfileUpdateRequest1.getCurrentCompany()).isEqualTo("DealShare");
        System.out.println("UserProfileResponse = " + userProfileUpdateRequest1);






    }




}