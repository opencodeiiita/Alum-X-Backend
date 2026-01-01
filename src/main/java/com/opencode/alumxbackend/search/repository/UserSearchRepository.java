package com.opencode.alumxbackend.search.repository;

import com.opencode.alumxbackend.users.dto.UserResponseDto;
import com.opencode.alumxbackend.users.model.User;
import java.util.List;

public interface UserSearchRepository {
    List<UserResponseDto> searchUsers(String query);
}
