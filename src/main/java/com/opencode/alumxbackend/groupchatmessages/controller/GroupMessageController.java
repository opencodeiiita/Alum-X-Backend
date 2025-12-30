package com.opencode.alumxbackend.groupchatmessages.controller;

import com.opencode.alumxbackend.groupchatmessages.dto.GroupChatMessageRepository;
import com.opencode.alumxbackend.groupchatmessages.dto.GroupMessageResponse;
import com.opencode.alumxbackend.groupchatmessages.dto.SendGroupMessageRequest;
import com.opencode.alumxbackend.groupchatmessages.service.GroupMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupMessageController {

    private final GroupMessageService service;


    // user id sends mesesage to a group using a group id
    @PostMapping(value="/{groupId}/messages",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> sendMessage(
            @PathVariable Long groupId,
            @RequestBody @Valid SendGroupMessageRequest request
    ) {
        return ResponseEntity.ok(service.sendMessage(groupId, request));
    }
    // working: keeping this the same 
    @GetMapping("/{groupId}/messages")
    public List<GroupMessageResponse> getMessages(
            @PathVariable Long groupId,
            @RequestParam Long userId
    ) {
        return service.fetchMessages(groupId, userId);
    }
    // Add this new endpoint: OPEN API (no auth)
    @GetMapping("/{groupId}/messages")
    public ResponseEntity<List<GroupMessageResponse>> getAllGroupMessages(
            @PathVariable Long groupId) {
        
        try {
            // Validate group exists
            if (!groupChatRepository.existsById(groupId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.emptyList()); // Empty list for non-existent group
            }
            
            List<GroupMessageResponse> messages = service.getAllGroupMessages(groupId);
            return ResponseEntity.ok(messages); // Clean list response
        // Handle exceptions
        } catch (GroupNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.emptyList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.emptyList());
        }
    }

}
