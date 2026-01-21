package com.opencode.alumxbackend.search.service;

import com.opencode.alumxbackend.users.dto.UserResponseDto;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.model.UserRole;
import com.opencode.alumxbackend.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserSearchServiceTest {

    @Autowired
    private UserSearchService service;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup(){

        User user = User.builder()
                .username("hasan")
                .name("Hasan Ravda")
                .email("hasan@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.STUDENT)
                .profileCompleted(false)
                .build();

        userRepository.save(user);

        User user1 = User.builder()
                .username("Gaurav")
                .name("Gaurav Chhetri")
                .email("ife2022004@iiita.ac.in")
                .passwordHash(passwordEncoder.encode("password"))
                .role(UserRole.STUDENT)
                .profileCompleted(false)
                .build();

        userRepository.save(user1);


    }

    @Test
    @DisplayName("Service Layer TEst : shouold return correct user from the DB by username")
    void searchByUsername() {

        List<UserResponseDto> result = service.search("ga");

        assertFalse(result.isEmpty());
        System.out.println("=====================================================");
        System.out.println(result);
    }
}
