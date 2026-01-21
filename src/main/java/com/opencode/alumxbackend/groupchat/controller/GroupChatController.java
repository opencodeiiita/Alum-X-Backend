package com.opencode.alumxbackend.groupchat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opencode.alumxbackend.groupchat.dto.GroupChatRequest;
import com.opencode.alumxbackend.groupchat.dto.GroupChatResponse;
import com.opencode.alumxbackend.groupchat.model.GroupChat;
import com.opencode.alumxbackend.groupchat.service.GroupChatService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/group-chats")
@RequiredArgsConstructor
public class GroupChatController {

    private final GroupChatService service;

    // Create a group
    // working
    @PostMapping
    public ResponseEntity<GroupChatResponse> createGroup(@Valid @RequestBody GroupChatRequest request) {
        GroupChat group = service.createGroup(request);
        return ResponseEntity.ok(mapToResponse(group));
    }

    // Get group by groupId
    //working
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupChatResponse> getGroupById(@PathVariable Long groupId) {
        GroupChat group = service.getGroupById(groupId);
        return ResponseEntity.ok(mapToResponse(group));
    }

    // Get all groups for a user
    //working
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getGroupsForUser(@PathVariable Long userId) {
        var groups = service.getGroupsForUser(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(groups);
    }

    // Add user to group (admin/owner only)
    @PostMapping("/{groupId}/add-user/{userId}")
    public ResponseEntity<GroupChatResponse> addUserToGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        
        GroupChat updatedGroup = service.addUserToGroup(groupId, userId);
        return ResponseEntity.ok(mapToResponse(updatedGroup));
    }

    // Remove user to group (admin/owner only)
    @PostMapping("/{groupId}/remove-user/{userId}")
    public ResponseEntity<GroupChatResponse> removeUserFromGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        
        GroupChat updatedGroup = service.removeUserFromGroup(groupId, userId);
        return ResponseEntity.ok(mapToResponse(updatedGroup));
    }

    

    // Helper method to map entity → DTO
    private GroupChatResponse mapToResponse(GroupChat group) {
        return GroupChatResponse.builder()
                .groupId(group.getGroupId())
                .name(group.getGroupName())
                .participants(group.getParticipants())
                .build();
    }
}

/*{
  "groupId": 6,
  "name": "My ggs",
  "participants": [
    {
      "id": 13,
      "userId": 67,
      "username": "som"
    },
    {
      "id": 14,
      "userId": 68,
      "username": "jj"
    }
  ]
}
*/
