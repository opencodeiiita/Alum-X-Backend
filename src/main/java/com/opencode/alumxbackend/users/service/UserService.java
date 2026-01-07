package com.opencode.alumxbackend.users.service;

import com.opencode.alumxbackend.users.dto.UserProfileResponse;
import com.opencode.alumxbackend.users.dto.UserProfileUpdateRequest;
import com.opencode.alumxbackend.users.dto.UserRequest;
import com.opencode.alumxbackend.users.dto.UserResponseDto;
import com.opencode.alumxbackend.users.model.User;

import java.util.List;

public interface UserService {
    User createUser(UserRequest request);
    UserProfileResponse getUserProfile(Long id);
    List<UserResponseDto> getAllUsers();
    UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request);
}
