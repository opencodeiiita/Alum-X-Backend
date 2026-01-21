package com.opencode.alumxbackend.groupchatmessages.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.opencode.alumxbackend.groupchatmessages.dto.GroupMessageSearchRequest;
import com.opencode.alumxbackend.groupchatmessages.dto.GroupMessageSearchResponse;
import com.opencode.alumxbackend.groupchatmessages.service.GroupMessageService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/group-chats")
@RequiredArgsConstructor
@Validated
public class GroupMessageSearchController {

    private final GroupMessageService groupMessageService;

    @GetMapping("/{groupId}/messages/search")
    public ResponseEntity<GroupMessageSearchResponse> searchMessage(
            @PathVariable Long groupId,
            @RequestHeader("X-USER-ID") Long userId,

            @RequestParam
            @NotBlank(message = "query is required")
            @Size(max = 16, message = "query length should be less than 100")
            String query,
            
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "page must be >= 0")
            Integer page,

            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "size must be >= 1")
            @Max(value = 50, message = "size must be <= 50")
            Integer size
        ) {

        GroupMessageSearchRequest request = GroupMessageSearchRequest.builder()
            .query(query)
            .page(page)
            .size(size)
            .build();

        GroupMessageSearchResponse messages = groupMessageService.searchForMessage(groupId, userId, request);
        return ResponseEntity.ok(messages);
    }
}
