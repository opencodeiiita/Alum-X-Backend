package com.opencode.alumxbackend.groupchatreadreceipt.controller;

import com.opencode.alumxbackend.groupchatreadreceipt.dto.GroupReadRequest;
import com.opencode.alumxbackend.groupchatreadreceipt.dto.GroupReadResponse;
import com.opencode.alumxbackend.groupchatreadreceipt.model.GroupReadState;
import com.opencode.alumxbackend.groupchatreadreceipt.service.GroupReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groupsread")
@RequiredArgsConstructor
public class GroupReadController {
    private final GroupReadService service;


    @PostMapping("/{groupId}/read")
    public ResponseEntity<GroupReadResponse> updateRead(
            @PathVariable Long groupId,
            @RequestBody GroupReadRequest request) {

        GroupReadResponse response = service.updateLastRead(groupId, request.getUserId(), request.getLastReadMessageId());
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{groupId}/last-read/{userId}")
    public ResponseEntity<GroupReadResponse> getLastRead(
            @PathVariable Long groupId,
            @PathVariable Long userId) {

        GroupReadResponse response = service.getLastReadMessage(groupId, userId);
        return ResponseEntity.ok(response);
    }

}
