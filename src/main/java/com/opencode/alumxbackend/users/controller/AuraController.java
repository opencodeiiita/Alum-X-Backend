package com.opencode.alumxbackend.users.controller;

import com.opencode.alumxbackend.users.dto.AuraResponse;
import com.opencode.alumxbackend.users.service.AuraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class AuraController {

    private final AuraService auraService;


    public AuraController(AuraService auraService) {
        this.auraService = auraService;
    }

    @GetMapping("/{userId}/aura")
    public ResponseEntity<AuraResponse> getUserAura(@PathVariable Long userId){
        AuraResponse aura = auraService.getUserAura(userId);
        return ResponseEntity.ok(aura);
    }
}
