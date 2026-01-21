package com.opencode.alumxbackend.search.service;

import com.opencode.alumxbackend.users.dto.UserResponseDto;
import java.util.List;

public interface UserSearchService {
    List<UserResponseDto> search(String query);
}
