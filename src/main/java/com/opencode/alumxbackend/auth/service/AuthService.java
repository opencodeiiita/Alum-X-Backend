package com.opencode.alumxbackend.auth.service;

import com.opencode.alumxbackend.auth.dto.LoginRequest;
import com.opencode.alumxbackend.auth.dto.LoginResponse;
import com.opencode.alumxbackend.auth.dto.RegisterRequest;
import com.opencode.alumxbackend.auth.dto.RegisterResponse;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
    RegisterResponse register(RegisterRequest registerRequest);
}
