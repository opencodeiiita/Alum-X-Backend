package com.opencode.alumxbackend.groupchatmessages.controller;

import com.opencode.alumxbackend.groupchatmessages.dto.GroupMessageResponse;
import com.opencode.alumxbackend.groupchatmessages.dto.SendGroupMessageRequest;
import com.opencode.alumxbackend.groupchatmessages.service.GroupMessageService;
import com.opencode.alumxbackend.groupchatreadreceipt.service.GroupReadService;
import jakarta.validation.Valid;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupMessageController {

    private final GroupMessageService service;
     private final GroupReadService groupReadService;

    // user id sends mesesage to a group using a group id
    @PostMapping(value="/{groupId}/messages",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> sendMessage(
            @PathVariable Long groupId,
            @RequestBody @Valid SendGroupMessageRequest request
    ) {
        GroupMessageResponse message = service.sendMessage(groupId, request);
        groupReadService.updateLastRead(groupId, request.getUserId(), message.getId());
        return ResponseEntity.ok(message);
    }
    
    // working: keeping this the same 
    @GetMapping("/{groupId}/messages/user")
    public List<GroupMessageResponse> getMessages(
            @PathVariable Long groupId,
            @RequestHeader("X-USER-ID") Long userId
    ) {
        return service.fetchMessages(groupId, userId);
    }
    @DeleteMapping("/{groupId}/messages/{messageId}")
public ResponseEntity<Void> deleteMessage(
        @PathVariable Long groupId,
        @PathVariable Long messageId,
        @RequestParam Long userId
) {
    service.deleteMessage(groupId, messageId, userId);
    return ResponseEntity.noContent().build();
}

    // Paginated endpoint with member authorization
    @GetMapping("/{groupId}/messages")
    public ResponseEntity<Page<GroupMessageResponse>> getGroupMessages(
            @PathVariable Long groupId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<GroupMessageResponse> messages = service.getGroupMessagesWithPagination(groupId, userId, page, size);
        return ResponseEntity.ok(messages);
    }

}
