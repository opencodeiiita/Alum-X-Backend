package com.opencode.alumxbackend.users.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opencode.alumxbackend.users.dto.ColorAwareAuraResponse;
import com.opencode.alumxbackend.users.dto.UserAuraResponse;
import com.opencode.alumxbackend.users.service.UserAuraService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAuraController {

    private final UserAuraService auraService;

    @GetMapping("/{userId}/aura")
    public ResponseEntity<UserAuraResponse> getAura(@PathVariable Long userId) {
        return ResponseEntity.ok(auraService.getAuraResponse(userId));
    }

    @GetMapping("/{userId}/aura/colors")
    public ResponseEntity<ColorAwareAuraResponse> getColorAwareAura(@PathVariable Long userId) {
        return ResponseEntity.ok(auraService.getColorAwareAuraResponse(userId));
    }
}
