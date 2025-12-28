package com.opencode.alumxbackend.users.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opencode.alumxbackend.users.dto.AuraResponse;
import com.opencode.alumxbackend.users.service.AuraService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuraController {

    private final AuraService auraService;

    @GetMapping("/{userId}/aura")
    public ResponseEntity<AuraResponse> getMethodName(@PathVariable Long id) {
        return ResponseEntity.ok(auraService.getAuraResponse(id));
    }
}
