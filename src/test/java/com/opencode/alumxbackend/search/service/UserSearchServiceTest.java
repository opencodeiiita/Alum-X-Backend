package com.opencode.alumxbackend.search.service;

import com.opencode.alumxbackend.users.dto.UserResponseDto;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.model.UserRole;
import com.opencode.alumxbackend.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserSearchServiceTest {

    @Autowired
    private UserSearchService service;

    @Autowired
    private UserRepository userRepository;

    @Test
    void searchByUsername() {
        User user = User.builder()
                .username("hasan")
                .name("Hasan Ravda")
                .email("hasan@test.com")
                .passwordHash("pass")
                .role(UserRole.STUDENT)
                .profileCompleted(true)
                .build();

        userRepository.save(user);

        List<UserResponseDto> result = service.search("has");

        assertFalse(result.isEmpty());
    }
}
