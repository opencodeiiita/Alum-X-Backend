package com.opencode.alumxbackend.users.service;

import com.opencode.alumxbackend.users.dto.ColorAwareAuraResponse;
import com.opencode.alumxbackend.users.dto.UserAuraResponse;

public interface UserAuraService {

    UserAuraResponse getAuraResponse(Long userId);
    
    ColorAwareAuraResponse getColorAwareAuraResponse(Long userId);
}
