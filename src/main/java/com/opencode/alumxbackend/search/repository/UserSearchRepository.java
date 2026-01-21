package com.opencode.alumxbackend.search.repository;

import com.opencode.alumxbackend.users.dto.UserResponseDto;
import java.util.List;

public interface UserSearchRepository {
    List<UserResponseDto> searchUsers(String query);
}
